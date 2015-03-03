package gl.glue.amqrouter;

import com.rabbitmq.client.*;
import com.typesafe.config.ConfigFactory;
import gl.glue.brahma.controllers.amq.MessageController;
import play.Application;
import play.Plugin;

import java.util.List;


public class AmqpPlugin extends Plugin {

    private final Application application;
    private Connection connection;
    private Channel channel;
    private String exchange;
    private Thread consumerThread;
    private volatile boolean exiting = false;

    public AmqpPlugin(Application application) {
        this.application = application;
    }

    @Override
    public void onStart() {
        String router = ConfigFactory.load().getString("amq.router");
        String uri = ConfigFactory.load().getString("amq.uri");
        exchange = ConfigFactory.load().getString("amq.exchange");
        List<String> bindings = ConfigFactory.load().getStringList("amq.bindings");

        try {
            Class.forName(router); // Calls its static initialization
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Router class not found.", e);
        }

        ConnectionFactory factory = new ConnectionFactory();
        try {
            factory.setUri(uri);
            connection = factory.newConnection();
            channel = connection.createChannel();
            channel.exchangeDeclare(exchange, "topic", true);
            String queue = channel.queueDeclare().getQueue();
            for (String binding: bindings) {
                channel.queueBind(queue, exchange, binding);
            }

            QueueingConsumer consumer = new QueueingConsumer(channel);
            channel.basicConsume(queue, true, consumer);

            Runnable r = () -> {
                while (!exiting) {
                    try {
                        QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                        Router.route(delivery);
                    } catch (InterruptedException e) { }
                }
            };
            consumerThread = new Thread(r);
            consumerThread.start();

            Router.on("JOIN").routeTo(MessageController::echoBack);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onStop(){
        Router.clear();
        try {
            exiting = true;
            consumerThread.interrupt();
            consumerThread.join();
            channel.close();
            connection.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean enabled() {
        return (application.configuration().keys().contains("amq.router") &&
                application.configuration().keys().contains("amq.uri") &&
                application.configuration().keys().contains("amq.exchange") &&
                application.configuration().keys().contains("amq.bindings"));
    }

    Channel getChannel() {
        return channel;
    }

    String getExchange() {
        return exchange;
    }

}

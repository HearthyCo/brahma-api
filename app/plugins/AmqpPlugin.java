package plugins;

import com.rabbitmq.client.*;
import com.typesafe.config.ConfigFactory;
import play.Application;
import play.Plugin;

import java.io.IOException;
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
        String uri = ConfigFactory.load().getString("rabbitmq.uri");
        exchange = ConfigFactory.load().getString("rabbitmq.exchange");
        List<String> bindings = ConfigFactory.load().getStringList("rabbitmq.bindings");

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
                        String message = new String(delivery.getBody());
                        String routingKey = delivery.getEnvelope().getRoutingKey();
                        handleMessage(routingKey, message);
                    } catch (InterruptedException e) { }
                }
            };
            consumerThread = new Thread(r);
            consumerThread.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onStop(){
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
        return (application.configuration().keys().contains("rabbitmq.uri") &&
                application.configuration().keys().contains("rabbitmq.exchange") &&
                application.configuration().keys().contains("rabbitmq.bindings"));
    }

    public void sendMessage(String routingKey, String message) {
        AMQP.BasicProperties props = new AMQP.BasicProperties();
        try {
            channel.basicPublish(exchange, routingKey, props, message.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleMessage(String routingKey, String message) {
        System.out.println(" [x] Received '" + routingKey + "':'" + message + "'");
        if (!routingKey.equals("repeat")) sendMessage("repeat", message); // TODO: this is just a demo
    }

}

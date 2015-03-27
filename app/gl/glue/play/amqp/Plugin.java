package gl.glue.play.amqp;

import com.rabbitmq.client.*;
import com.typesafe.config.ConfigFactory;
import gl.glue.brahma.controllers.amq.MessageController;
import play.Application;

import java.util.List;

/**
 * A Play Plugin for AMQP messaging integration.
 * <p>
 * The following settings can be specified on your <i>application.conf</i>:
 * <ul>
 * <li><i>amqp.router</i>: The class containing your router.
 * <li><i>amqp.uri</i>: The AMQP URI string used to connect to the AMQP server.
 * <li><i>amqp.exchange</i>: The exchange to use to receive and send messages.
 * <li><i>amqp.bindings</i>: A list of the bindings to subscribe to.
 * </ul><p>
 * To enable the plugin, configure the settings, and add a reference to it on your <i>play.plugins</i> file.
 * <p>
 * The router class only needs to have a static initialization calling {@link Router#on} with each route.
 * Routes can contain wildcards "*" and "#", as specified on the AMQP documentation.
 * The controller method receives a QueueingConsumer.Delivery and returns nothing. Eg:
 * <pre>{@code
 * public class ExampleRouter {
 *     static {
 *         Router.on("PING").routeTo(Controller::ping);
 *         Router.on("#").routeTo(Controller::log);
 *     }
 * }
 * }</pre>
 */
public class Plugin extends play.Plugin {

    private final Application application;
    private Connection connection;
    private Channel channel;
    private String exchange;
    private Thread consumerThread;
    private volatile boolean exiting = false;

    /**
     * Called by Play. Creates a plugin instance, associating it with the current application.
     * @param application The current application.
     */
    public Plugin(Application application) {
        this.application = application;
    }

    /**
     * Called by Play. Prepares the router, connects to the AMQP server, and creates a message consumer thread.
     */
    @Override
    public void onStart() {
        String router = ConfigFactory.load().getString("amqp.router");
        String uri = ConfigFactory.load().getString("amqp.uri");
        exchange = ConfigFactory.load().getString("amqp.exchange");
        List<String> bindings = ConfigFactory.load().getStringList("amqp.bindings");

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

    /**
     * Called by Play. Stops all the AMQP activity and disconnects from the server.
     */
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

    /**
     * Called by Play. Checks if the plugin is enabled (ie: the settings have been specified).
     * @return true if configured, false otherwise.
     */
    @Override
    public boolean enabled() {
        return (application.configuration().keys().contains("amqp.router") &&
                application.configuration().keys().contains("amqp.uri") &&
                application.configuration().keys().contains("amqp.exchange") &&
                application.configuration().keys().contains("amqp.bindings") &&
                !application.configuration().getBoolean("amqp.disabled", false));
    }

    /**
     * Returns the channel assigned by the AMQP server.
     * @return The current channel.
     */
    Channel getChannel() {
        return channel;
    }

    /**
     * Returns the exchange we're currently subscribed to.
     * @return The current exchange.
     */
    String getExchange() {
        return exchange;
    }

}

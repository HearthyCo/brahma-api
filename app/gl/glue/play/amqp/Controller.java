package gl.glue.play.amqp;

import com.rabbitmq.client.AMQP;
import play.Play;

import java.io.IOException;

/**
 * Base class for the controllers.
 * <p>
 * It is not required to extend it from the controllers, but doing so provides useful helper methods.
 */
public class Controller {

    private static Plugin plugin;

    static {
        plugin = Play.application().plugin(Plugin.class);
    }

    /**
     * Sends a message to the current exchange.
     * @param routingKey The routing key for the message.
     * @param message The message body.
     */
    public static void sendMessage(String routingKey, String message) {
        AMQP.BasicProperties props = new AMQP.BasicProperties();
        try {
            plugin.getChannel().basicPublish(plugin.getExchange(), routingKey, props, message.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

package gl.glue.amqrouter;

import com.rabbitmq.client.AMQP;
import play.Play;

import java.io.IOException;

public class AmqController {

    private static AmqpPlugin plugin;

    static {
        plugin = Play.application().plugin(AmqpPlugin.class);
    }

    protected static void sendMessage(String routingKey, String message) {
        AMQP.BasicProperties props = new AMQP.BasicProperties();
        try {
            plugin.getChannel().basicPublish(plugin.getExchange(), routingKey, props, message.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

package gl.glue.brahma.controllers.amq;

import com.rabbitmq.client.QueueingConsumer;
import gl.glue.play.amqp.Controller;

public class MessageController extends Controller {

    public static void doNothing(QueueingConsumer.Delivery delivery) {
        String key = delivery.getEnvelope().getRoutingKey();
        String message = new String(delivery.getBody());
        System.out.println("Discard: [" + key + "] " + message);
    }

    public static void echoBack(QueueingConsumer.Delivery delivery) {
        String key = delivery.getEnvelope().getRoutingKey();
        String message = new String(delivery.getBody());
        System.out.println("Echo: [" + key + "] " + message);
        sendMessage("repeat", message);
    }

}

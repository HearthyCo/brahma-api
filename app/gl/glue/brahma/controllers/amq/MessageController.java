package gl.glue.brahma.controllers.amq;

import com.rabbitmq.client.QueueingConsumer;
import gl.glue.brahma.service.SessionService;
import gl.glue.play.amqp.Controller;
import play.db.jpa.JPA;
import play.libs.Json;

import java.util.List;
import java.util.Map;

public class MessageController extends Controller {

    private static SessionService sessionService = new SessionService();

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

    public static void listSessions(QueueingConsumer.Delivery delivery) {
        // Answer with a list of all the open sessions and their participants
        JPA.withTransaction(() -> {
            Map<Integer, List<Integer>> participants = sessionService.getCurrentSessionsParticipants();
            sendMessage("sessions.users",
                    Json.newObject()
                            .putPOJO("sessions", Json.toJson(participants))
                            .toString());
        });
    }

}

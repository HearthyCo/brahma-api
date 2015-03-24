package gl.glue.brahma.util;

import com.fasterxml.jackson.databind.node.ObjectNode;
import gl.glue.brahma.model.user.User;
import gl.glue.play.amqp.Controller;
import play.libs.Json;

public class Notificator {

    public enum NotificationEvents {
        USER_REGISTER("user.register"),
        USER_RECOVER("user.recover");

        private String event;

        NotificationEvents(String event) {
            this.event = event;
        }

        public String getEvent() { return this.event; }

        @Override
        public String toString() { return event; }
    }

    public static void send(User user, NotificationEvents event, ObjectNode meta) {
        ObjectNode payload = Json.newObject();

        payload.put("user", Json.toJson(user));
        ((ObjectNode)payload.get("user")).put("meta", user.getMeta()); // Skip meta fields erasure

        if(meta == null) meta = Json.newObject();
        meta.put("type", event.toString());

        payload.put("meta", meta);

        Controller.sendMessage(event.getEvent(), payload.toString());
    }

    public static void send(User user, NotificationEvents event) {
        send(user, event, Json.newObject());
    }

}

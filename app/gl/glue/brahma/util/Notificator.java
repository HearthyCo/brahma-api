package gl.glue.brahma.util;

import com.fasterxml.jackson.databind.JsonNode;
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

    public static void send(User user, NotificationEvents event, ObjectNode opts) {
        ObjectNode defaults = Json.newObject();
        defaults.put("user", Json.toJson(user));
        ((ObjectNode)defaults.get("user")).put("meta", user.getMeta()); // Skip meta fields erasure
        defaults.put("type", event.toString());

        if(opts != null) opts = Json.newObject();

        JsonNode payload = JsonUtils.merge(defaults, opts);

        Controller.sendMessage(event.getEvent(), payload.toString());
    }

    public static void send(User user, NotificationEvents event) {
        send(user, event, null);
    }

}

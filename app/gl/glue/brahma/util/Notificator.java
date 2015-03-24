package gl.glue.brahma.util;

import com.fasterxml.jackson.databind.node.ObjectNode;
import gl.glue.brahma.model.user.User;
import gl.glue.play.amqp.Controller;
import play.libs.Json;

public class Notificator {

    public enum NotificationEvents {
        USER_REGISTER("user.register"),
        USER_RECOVER_PASSWORD("user.recoverPassword"),
        USER_CONFIRM_PASSWORD("user.confirmPassword"),
        USER_CONFIRM("user.confirm");

        private String event;

        NotificationEvents(String event) {
            this.event = event;
        }

        public String getEvent() { return this.event; }

        @Override
        public String toString() { return event; }
    }

    public static void send(User user, NotificationEvents event, ObjectNode vars) {
        ObjectNode payload = Json.newObject();

        payload.put("user", Json.toJson(user));
        ((ObjectNode)payload.get("user")).put("meta", user.getMeta()); // Skip meta fields erasure

        if(vars == null) vars = Json.newObject();
        vars.put("type", event.toString());

        payload.put("vars", vars);

        Controller.sendMessage(event.getEvent(), payload.toString());
    }

    public static void send(User user, NotificationEvents event) {
        send(user, event, Json.newObject());
    }

}

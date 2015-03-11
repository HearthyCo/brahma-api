package gl.glue.brahma.util;

import com.fasterxml.jackson.databind.node.ObjectNode;
import gl.glue.brahma.model.user.User;
import gl.glue.play.amqp.Controller;
import play.libs.Json;

public class Mailer {

    public enum MailTemplate {
        REGISTER_CONFIRM_MAIL,
        RECOVER_CONFIRM_MAIL,
    }

    public static void send(User user, MailTemplate template) {
        ObjectNode payload = Json.newObject();
        payload.put("user", Json.toJson(user));
        payload.put("type", template.toString());
        Controller.sendMessage("mail." + template, payload.toString());
    }

}

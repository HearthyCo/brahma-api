package gl.glue.brahma.util;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gl.glue.brahma.model.user.Admin;
import gl.glue.brahma.model.user.User;
import gl.glue.brahma.service.SessionService;
import gl.glue.brahma.service.UserService;
import org.apache.commons.lang3.StringUtils;
import play.libs.Json;

import java.util.List;

public class SignatureHelper {

    private static SessionService sessionService = new SessionService();
    private static UserService userService = new UserService();

    public static void addSignatures(ObjectNode node, int uid) {
        List<Integer> sessions = sessionService.getUserSessionIds(uid);
        String sessionsStr = "[" + StringUtils.join(sessions, ",") + "]";

        ArrayNode sign = new ArrayNode(JsonNodeFactory.instance);
        User user = userService.getById(uid);
        if (sessions.size() != 0 || !(user instanceof Admin)) {
            sign.addPOJO(Json.newObject()
                .put("id", "sessions")
                .put("signature", Signing.sign(sessionsStr))
                .putPOJO("value", Json.toJson(sessions)));
        }

        sign.addPOJO(Json.newObject()
            .put("id", "userId")
            .put("signature", Signing.sign(Integer.toString(uid)))
            .put("value", uid));

        String role = user.getUserType();
        sign.addPOJO(Json.newObject()
                .put("id", "userRole")
                .put("signature", Signing.sign(role))
                .put("value", role));

        node.putPOJO("sign", sign);
    }

}

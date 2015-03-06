package gl.glue.brahma.util;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gl.glue.brahma.service.SessionService;
import org.apache.commons.lang3.StringUtils;
import play.libs.Json;

import java.util.List;

public class SignatureHelper {

    private static SessionService sessionService = new SessionService();

    public static void addSignatures(ObjectNode node, int uid) {
        List<Integer> sessions = sessionService.getUserSessionIds(uid);
        String sessionsStr = "[" + StringUtils.join(sessions, ",") + "]";

        node.putPOJO("sign", new ArrayNode(JsonNodeFactory.instance)
            .addPOJO(Json.newObject()
                .put("id", "sessions")
                .put("signature", Signing.sign(sessionsStr))
                .putPOJO("value", Json.toJson(sessions)))
            .addPOJO(Json.newObject()
                .put("id", "userId")
                .put("signature", Signing.sign(Integer.toString(uid)))
                .put("value", uid))
        );
    }

}

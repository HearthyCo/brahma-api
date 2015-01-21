package gl.glue.brahma.model.session;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;

public class SessionUtils {
    /**
     * Create Session JSON object from object array from Session DAO
     * @param sessionObject Object array to read session values
     * @return ObjectNode with a session with values passed in sessionObject.
     */
    public static ObjectNode createSessionObject(Object[] sessionObject) {
        JsonNode session = Json.toJson(sessionObject);
        ObjectNode result = Json.newObject();

        result.put("id", session.get(0).asText());
        if(!session.get(1).isNull()) result.put("title", session.get(1).asText());
        if(!session.get(2).isNull()) result.put("startDate", session.get(2));

        boolean isNew = false;
        if (!session.get(3).isNull()) isNew = session.get(3).asBoolean();
        result.put("isNew", isNew);
        if(!session.get(4).isNull()) result.put("state", session.get(4).asText().toLowerCase());

        return result;
    }
}

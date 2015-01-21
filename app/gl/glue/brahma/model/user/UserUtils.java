package gl.glue.brahma.model.user;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;

public class UserUtils {
    /**
     * Create User JSON object from object array from User DAO
     * @param userObject Object array to read user values
     * @return ObjectNode with a user with values passed in sessionObject.
     */
    public static ObjectNode createUserObject(Object[] userObject) {
        JsonNode user = Json.toJson(userObject);
        ObjectNode result = Json.newObject();

        result.put("id", user.get(0).asInt());
        result.put("login", user.get(1).asText());
        if(!user.get(2).isNull()) result.put("name", user.get(2).asText());
        if(!user.get(3).isNull()) result.put("surname1", user.get(3).asText());
        if(!user.get(4).isNull()) result.put("surname2", user.get(4).asText());
        if(!user.get(5).isNull()) result.put("avatar", user.get(5).asText());
        if(!user.get(6).isNull()) result.put("service", user.get(6).asText());
        if(!user.get(8).isNull()) result.put("report", user.get(8).asText());

        return result;
    }
}

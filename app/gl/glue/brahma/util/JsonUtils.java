package gl.glue.brahma.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;

public class JsonUtils {

    public static ObjectNode simpleError(String status, String title) {
        ObjectNode result = Json.newObject();
        ArrayNode errors = result.arrayNode();
        result.put("errors", errors);
        ObjectNode error = Json.newObject();
        error.put("status", status);
        error.put("title", title);
        errors.add(error);
        return result;
    }

    public static ObjectNode noJsonBodyError() {
        return simpleError("400", "Expected a JSON body in the request.");
    }

    public static ObjectNode missingRequiredField(String field) {
        return simpleError("400", "Missing required field \"" + field + "\"");
    }

    /**
     * Check if a JSON request contains all the required fields.
     * @param json The request JSON
     * @param args The required fields (specified by strings such as "user.login")
     * @return An error for the first missing field, or null if no errors found.
     */
    public static ObjectNode checkRequiredFields(JsonNode json, String... args) {
        if (json == null) {
            return noJsonBodyError();
        } else {
            for (String arg : args) {
                System.out.println("Checking for: " + arg);
                JsonNode t = json;
                for (String field: arg.split("\\.")) {
                    if (!t.has(field)) {
                        return missingRequiredField(arg);
                    }
                    System.out.println("Found field: " + field);
                    t = t.get(field);
                }
            }
        }
        return null;
    }

    public static ObjectNode handleDeserializeException(Throwable e, String where) {
        String msg = "Unknown error while decoding user.";
        if (e.getCause() instanceof UnrecognizedPropertyException) {
            msg = "Received unrecognized field: \"";
            msg += ((UnrecognizedPropertyException)e.getCause()).getPropertyName();
            msg += "\"";
        } else if (e.getCause() instanceof InvalidFormatException) {
            msg = "Invalid value for field: \"";
            msg += ((InvalidFormatException)e.getCause()).getPath().get(0).getFieldName();
            msg += "\"";
        }
        return simpleError("400", msg);
    }

}

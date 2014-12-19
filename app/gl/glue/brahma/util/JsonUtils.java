package gl.glue.brahma.util;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.api.libs.iteratee.Enumerator;
import play.libs.Json;
import play.mvc.Result;
import scala.collection.mutable.WrappedArray;

import java.lang.reflect.Field;
import java.util.*;

public class JsonUtils {

    /**
     * Generates a simple JSON error response for the specified error.
     * @param status The HTTP status code.
     * @param title A human-readable error description.
     * @return A JSON containing the specified error.
     */
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

    /**
     * Generates a JSON error response for body decode errors.
     * @return A JSON detailing the error.
     */
    public static ObjectNode noJsonBodyError() {
        return simpleError("400", "Expected a JSON body in the request.");
    }

    /**
     * Generates a JSON error response for a missing field.
     * @param field The name of the field.
     * @return A JSON detailing the error.
     */
    public static ObjectNode missingRequiredField(String field) {
        return simpleError("400", "Missing required field \"" + field + "\"");
    }

    /**
     * Check if a JSON request contains all the required fields.
     * @param json The request JSON.
     * @param args The required fields (specified by strings such as "user.login").
     * @return An error for the first missing field, or null if no errors found.
     */
    public static ObjectNode checkRequiredFields(JsonNode json, String... args) {
        if (json == null) {
            return noJsonBodyError();
        } else {
            for (String arg : args) {
                JsonNode t = json;
                for (String field: arg.split("\\.")) {
                    if (!t.has(field)) {
                        return missingRequiredField(arg);
                    }
                    t = t.get(field);
                }
            }
        }
        return null;
    }

    /**
     * Generates a JSON response for the given error during the deserialization.
     * @param e The exception thrown by Jackson.
     * @param where The entity name that was being decoded.
     * @return A JSON detailing the error.
     */
    public static ObjectNode handleDeserializeException(Throwable e, String where) {
        String msg = "Unknown error while decoding " + where + ".";
        if (e.getCause() instanceof UnrecognizedPropertyException) {
            msg = "Received unrecognized field: \"";
            msg += ((UnrecognizedPropertyException)e.getCause()).getPropertyName();
            msg += "\"";
        } else if (e.getCause() instanceof InvalidFormatException) {
            msg = "Invalid value for field: \"";
            msg += ((InvalidFormatException)e.getCause()).getPath().get(0).getFieldName();
            msg += "\"";
        } else if (e.getCause() instanceof JsonMappingException) {
            msg = "Invalid value for field: \"";
            msg += ((JsonMappingException)e.getCause()).getPath().get(0).getFieldName();
            msg += "\"";
        }
        return simpleError("400", msg);
    }


    /**
     * Similar to {@link #cleanFields(ObjectNode, TreeMap)}, but receiving the allowed fields list as multiple parameters.
     * @param json The JSON object to clean. It will be modified in place.
     * @param allowed The list of allowed fields, specified as "path.to.leaf".
     * @return The same JSON object after cleaning it.
     */
    public static ObjectNode cleanFields(ObjectNode json, String... allowed) {
        TreeMap<String> map = new TreeMap<>();
        for (String s: allowed) map.add(s.split("\\."));
        return cleanFields(json, map);
    }

    /**
     * Similar to {@link #cleanFields(ObjectNode, TreeMap)}, but receiving the allowed fields list as a list of strings.
     * @param json The JSON object to clean. It will be modified in place.
     * @param allowed The list of allowed fields, specified as "path.to.leaf".
     * @return The same JSON object after cleaning it.
     */
    public static ObjectNode cleanFields(ObjectNode json, List<String> allowed) {
        TreeMap<String> map = new TreeMap<>();
        allowed.forEach(s -> map.add(s.split("\\.")));
        return cleanFields(json, map);
    }

    /**
     * Cleans a JSON object removing all the fields not on the allowed list.
     * @param json The JSON object to clean. It will be modified in place.
     * @param allowed The list of allowed fields.
     * @return The same JSON object after cleaning it.
     */
    public static ObjectNode cleanFields(ObjectNode json, TreeMap<String> allowed) {
        json.fields().forEachRemaining(i -> {
            String key = i.getKey();
            TreeMap<String> submap = allowed.get(key);
            if (submap != null) {
                if (i.getValue().isObject()) {
                    json.replace(key, cleanFields((ObjectNode)i.getValue(), submap));
                }
            } else {
                json.remove(key);
            }
        });
        return json;
    }

    /**
     * Extracts the JSON body of a Result object.
     * @param result The Result returned by the appllication.
     * @return The JSON it contained in the body, or null if not available.
     */
    @SuppressWarnings("unchecked")
    public static ObjectNode result2json(Result result) {
        Enumerator<byte[]> enumerator = result.toScala().body();
        /* No f***ing way I'm going to do all that Iteratee nonsense! */
        try {
            Field f = enumerator.getClass().getDeclaredFields()[0];
            WrappedArray<Object> wa = (WrappedArray<Object>)f.get(enumerator);
            Field f2 = wa.getClass().getDeclaredFields()[0];
            f2.setAccessible(true);
            byte[][] bytes = (byte[][])f2.get(wa);
            String body = new String(bytes[0]);
            return (ObjectNode)Json.parse(body);
        } catch (Exception e) {
            return null;
        }

    }

}

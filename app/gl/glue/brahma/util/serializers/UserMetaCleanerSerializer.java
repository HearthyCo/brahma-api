package gl.glue.brahma.util.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gl.glue.brahma.util.JsonUtils;
import gl.glue.brahma.util.ModelSecurity;
import play.libs.Json;

import java.io.IOException;

public class UserMetaCleanerSerializer extends JsonSerializer<JsonNode> {

    @Override
    public void serialize(JsonNode meta,
                          JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider)
            throws IOException {
        ObjectNode myMeta = Json.newObject();
        myMeta.put("meta", (ObjectNode) meta.deepCopy());
        JsonUtils.cleanFields(myMeta, ModelSecurity.USER_MODIFICABLE_FIELDS);
        jsonGenerator.writeTree(myMeta.get("meta"));
    }
}
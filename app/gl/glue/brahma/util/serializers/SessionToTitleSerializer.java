package gl.glue.brahma.util.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import gl.glue.brahma.model.session.Session;

import java.io.IOException;

public class SessionToTitleSerializer extends JsonSerializer<Session> {

    @Override
    public void serialize(Session session,
                          JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider)
            throws IOException {
        if (session != null) {
            jsonGenerator.writeString(session.getTitle());
        }
    }
}
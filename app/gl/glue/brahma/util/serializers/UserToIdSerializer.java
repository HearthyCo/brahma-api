package gl.glue.brahma.util.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import gl.glue.brahma.model.session.Session;
import gl.glue.brahma.model.user.User;

import java.io.IOException;

public class UserToIdSerializer extends JsonSerializer<User> {

    @Override
    public void serialize(User user,
                          JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider)
            throws IOException {
        if (user != null) {
            jsonGenerator.writeNumber(user.getId());
        }
    }
}
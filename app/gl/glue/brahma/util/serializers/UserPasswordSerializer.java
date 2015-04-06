package gl.glue.brahma.util.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import gl.glue.brahma.model.user.User;

import java.io.IOException;

public class UserPasswordSerializer extends JsonSerializer<String> {

    @Override
    public void serialize(String password,
                          JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider)
            throws IOException {
        jsonGenerator.writeString("");
    }
}
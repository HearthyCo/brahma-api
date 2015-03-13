package gl.glue.brahma.util.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import gl.glue.brahma.model.servicetype.ServiceType;

import java.io.IOException;

public class ServiceTypeToIdSerializer extends JsonSerializer<ServiceType> {

    @Override
    public void serialize(ServiceType serviceType,
                          JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider)
            throws IOException {
        if (serviceType != null) {
            jsonGenerator.writeNumber(serviceType.getId());
        }
    }
}
package gl.glue.brahma.service;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gl.glue.brahma.model.service.ServiceDao;
import gl.glue.brahma.model.servicetype.ServiceType;
import play.db.jpa.Transactional;
import play.libs.Json;

import java.util.List;

public class ServiceService {

    private ServiceDao serviceDao = new ServiceDao();

    /**
     * Find all services in database
     * @return An ObjectNode with an array of services with his name, mode and price
     */
    @Transactional
    public ObjectNode getServices() {

        List<ServiceType> serviceTypes = serviceDao.findServiceTypes();

        ObjectNode services = Json.newObject();
        for(ServiceType serviceType : serviceTypes) {
            String field = serviceType.getField().getName().toLowerCase();
            ArrayNode service = services.has(field) ? (ArrayNode) services.get(field) : new ArrayNode(JsonNodeFactory.instance);

            ObjectNode srv = Json.newObject();

            srv.put("name", serviceType.getName());
            srv.put("mode", String.valueOf(serviceType.getMode()));
            srv.put("price", serviceType.getPrice());

            service.add(srv);
            services.put(field, service);
        }

        ObjectNode result = Json.newObject();
        result.put("services", services);

        return result;
    }
}

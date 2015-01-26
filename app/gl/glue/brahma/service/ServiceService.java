package gl.glue.brahma.service;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gl.glue.brahma.model.service.Service;
import gl.glue.brahma.model.service.ServiceDao;
import gl.glue.brahma.model.servicetype.ServiceType;
import play.db.jpa.Transactional;
import play.libs.Json;

import java.util.List;

public class ServiceService {

    ServiceDao serviceDao = new ServiceDao();

    /**
     * Find all services in database
     * @return An ObjectNode with an array of services with his name, mode and price
     */
    @Transactional
    public ObjectNode getServices() {

        ArrayNode servicesArray = new ArrayNode(JsonNodeFactory.instance);
        List<Service> services = serviceDao.findServices();

        for(Service service : services) {
            ObjectNode srv = Json.newObject();
            ServiceType serviceType = service.getServiceType();

            srv.put("name", serviceType.getField().getName());
            srv.put("mode", String.valueOf(serviceType.getMode()));
            srv.put("price", serviceType.getPrice());

            servicesArray.add(srv);
        }

        ObjectNode result = Json.newObject();
        result.put("services", servicesArray);

        return result;
    }
}

package gl.glue.brahma.controllers.common;

import actions.BasicAuth;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gl.glue.brahma.model.servicetype.ServiceType;
import gl.glue.brahma.service.ServiceService;
import play.db.jpa.Transactional;
import play.libs.F;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.List;

public class ServiceController extends Controller {

    private static ServiceService serviceService = new ServiceService();

    /**
     * @api {get} /client/services Services
     *
     * @apiGroup Services
     * @apiName GetService
     * @apiDescription Returns a list of ServiceTypes available, optionally filtering them by field.
     *     Also available for professionals.
     *
     * @apiParam {Integer}   fid Field id {Optional}
     *
     * @apiSuccess {object}     List of ServiceTypes, keyed by field.
     * @apiSuccessExample {json} Success-Response:
     *     HTTP/1.1 200 OK
     *     {
     *          services: {
     *              "general": [
     *                  {
     *                      "id": 90300,
     *                      "price": 1500
     *                      "name": "Video Session",
     *                      "mode": "VIDEO",
     *                      "poolsize": 5
     *                  },
     *                  {
     *                      "id": 90301,
     *                      "price": 250
     *                      "name": "Chat",
     *                      "mode": "ASYNC",
     *                      "poolsize": 10
     *                  }
     *              ]
     *          }
     *     }
     *
     * @apiError UserNotLoggedIn User is not logged in.
     * @apiErrorExample {json} UserNotLoggedIn
     *      HTTP/1.1 401 Unauthorized
     *      {
     *          "status": "401",
     *          "title": "You are not logged in"
     *      }
     *
     * @apiVersion 0.1.0
     */
    @BasicAuth
    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public static Result getServices(final F.Option<Integer> fid) {
        List<ServiceType> serviceTypes;

        if (fid.isDefined()) {
            serviceTypes = serviceService.getServiceTypesByField(fid.get());
        } else {
            serviceTypes = serviceService.getAllServiceTypes();
        }

        ObjectNode services = Json.newObject();
        for(ServiceType serviceType : serviceTypes) {
            String field = serviceType.getField().getName().toLowerCase();
            ArrayNode service = services.has(field) ? (ArrayNode) services.get(field) : new ArrayNode(JsonNodeFactory.instance);

            ObjectNode srv = (ObjectNode) Json.toJson(serviceType);

            service.add(srv);
            services.put(field, service);
        }

        ObjectNode result = Json.newObject();
        result.put("services", services);

        return ok(result);
    }
}

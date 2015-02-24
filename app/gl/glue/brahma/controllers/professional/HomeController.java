package gl.glue.brahma.controllers.professional;

import actions.ProfessionalAuth;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gl.glue.brahma.model.service.Service;
import gl.glue.brahma.model.servicetype.ServiceType;
import gl.glue.brahma.model.session.Session;
import gl.glue.brahma.model.sessionuser.SessionUser;
import gl.glue.brahma.model.transaction.Transaction;
import gl.glue.brahma.model.user.User;
import gl.glue.brahma.service.ServiceService;
import gl.glue.brahma.service.SessionService;
import gl.glue.brahma.service.UserService;
import play.Logger;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HomeController extends Controller {

    private static SessionService sessionService = new SessionService();
    private static ServiceService serviceService = new ServiceService();

    /**
     * @api {get} /user/home Homepage
     *
     * @apiGroup Professional
     * @apiName GetHome
     * @apiDescription Collect all entities required to show in home view.
     *
     * @apiSuccess {Object}     sessions             Contains all user sessions grouped by state.
     * @apiSuccessExample {json} Success-Response
     *      HTTP/1.1 200 OK
     *      {
     *          "sessions": {
     *
     *          }
     *      }
     *
     * @apiError {Object} UserNotLoggedIn User is not logged in.
     * @apiErrorExample {json} UserNotLoggedIn
     *      HTTP/1.1 401 Unauthorized
     *      {
     *          "status": "401",
     *          "title": "You are not logged in"
     *      }
     *
     * @apiError {Object} UnauthorizedUser User is not a professional.
     * @apiErrorExample {json} UnauthorizedUser
     *      HTTP/1.1 403 Unauthorized
     *      {
     *          "status": "403",
     *          "title": "Unauthorized"
     *      }
     *
     * @apiVersion 0.1.0
     */
    @ProfessionalAuth
    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public static Result getHome() {

        int uid = Integer.parseInt(session("id"));

        // Create State Session List Array for iterate and pass DAO function a Session.State ArrayList
        List<Service> services = serviceService.getServicesOfUser(uid);
        Set<Session.State> states = EnumSet.of(Session.State.UNDERWAY, Session.State.CLOSED);

        ArrayNode sessions = new ArrayNode(JsonNodeFactory.instance);
        ArrayNode serviceTypes = new ArrayNode(JsonNodeFactory.instance);
        ObjectNode sessionsByServiceType = Json.newObject();

        Map<Integer, Integer> poolsSize = sessionService.getPoolsSize();

        // Iterate State Session List Array
        for (Service service : services) {
            ServiceType serviceType = service.getServiceType();
            ObjectNode serviceTypeObject = (ObjectNode) Json.toJson(serviceType);
            int serviceTypeId = serviceType.getId();

            int queue = poolsSize.containsKey(serviceTypeId) ? poolsSize.get(serviceTypeId) : 0;
            serviceTypeObject.put("waiting", queue);

            serviceTypes.add(serviceTypeObject);

            List<SessionUser> sessionUsers = sessionService.getUserSessionsByService(uid, serviceTypeId, states);

            ArrayNode sessionsThisService = new ArrayNode(JsonNodeFactory.instance);
            for(SessionUser sessionUser : sessionUsers) {
                Session session = sessionUser.getSession();

                sessions.add(Json.toJson(session));
                sessionsThisService.add(session.getId());
            }

            sessionsByServiceType.put(Integer.toString(serviceTypeId), sessionsThisService);
        }

        return ok(Json.newObject()
                .putPOJO("home", Json.newObject()
                        .putPOJO("serviceTypeSessions", sessionsByServiceType))
                .putPOJO("sessions", sessions)
                .putPOJO("servicetypes", serviceTypes));
    }
}

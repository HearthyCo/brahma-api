package gl.glue.brahma.controllers.professional;

import actions.ProfessionalAuth;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gl.glue.brahma.model.historyentry.HistoryEntry;
import gl.glue.brahma.model.service.Service;
import gl.glue.brahma.model.servicetype.ServiceType;
import gl.glue.brahma.model.session.Session;
import gl.glue.brahma.model.sessionuser.SessionUser;
import gl.glue.brahma.model.user.User;
import gl.glue.brahma.service.HistoryService;
import gl.glue.brahma.service.ServiceService;
import gl.glue.brahma.service.SessionService;
import gl.glue.brahma.util.JsonUtils;
import gl.glue.brahma.util.SignatureHelper;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.*;
import java.util.stream.Collectors;

public class SessionController extends Controller {

    private static SessionService sessionService = new SessionService();
    private static ServiceService serviceService = new ServiceService();
    private static HistoryService historyService = new HistoryService();

    /**
     * @api {post} /client/session/assignPool Assign session from pool
     *
     * @apiGroup Session
     * @apiName AssignSessionFromPool
     * @apiDescription Assign a session from one service type pool to the current user.
     *
     * @apiParam {Integer} serviceType Service type id.
     * @apiParamExample {json} Request-Example
     *      {
     *          "serviceType": 90302,
     *      }
     *
     * @apiSuccess {Object} session Info about the assigned session.
     * @apiSuccessExample {json} Success-Response:
     *      HTTP/1.1 200 OK
     *      {
     *          "session": {
     *              "id": 90712,
     *              "title": "testPool1",
     *              "startDate": 1423670400000,
     *              "endDate": 1423671300000,
     *              "state": "UNDERWAY",
     *              "meta": {},
     *              "timestamp": 1418626800000
     *         }
     *     }
     *
     * @apiError TargetNotFound No suitable sessions have been found
     * @apiErrorExample {json} TargetNotFound
     *      HTTP/1.1 404 Not Found
     *      {
     *          "status": "404",
     *          "title": "Couldn't assign any session"
     *      }
     *
     * @apiError UserNotLoggedIn User is not logged in.
     * @apiErrorExample {json} UserNotLoggedIn
     *      HTTP/1.1 401 Unauthorized
     *      {
     *          "status": "401",
     *          "title": "You are not logged in"
     *      }
     *
     * @apiError MissingRequiredField Missing required field
     * @apiErrorExample {json} MissingRequiredField
     *      HTTP/1.1 400 BadRequest
     *      {
     *          "status": "400",
     *          "title": "Missing required field \"serviceType\""
     *      }
     *
     * @apiVersion 0.1.0
     */
    @ProfessionalAuth
    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public static Result assignSessionFromPool() {
        int uid = Integer.parseInt(session("id"));

        JsonNode json = request().body().asJson();
        if (!json.has("serviceType")) {
            return badRequest(JsonUtils.missingRequiredField("serviceType"));
        }
        int serviceTypeId = json.get("serviceType").asInt();

        Session session = sessionService.assignSessionFromPool(uid, serviceTypeId);
        if (session == null) {
            return notFound(JsonUtils.simpleError("404", "Couldn't assign any session"));
        }

        return getAssignedSessions();
    }

    /**
     * @api {post} /professional/sessions/assigned/ Return assigned sessions
     *
     * @apiGroup Session
     * @apiName GetAssignedSessions
     * @apiDescription Return assigned sessions of current user.
     *
     * @apiParam {Integer} serviceTypeId Service type id.
     *
     * @apiSuccess {Object} session Info about the assigned session.
     * @apiSuccessExample {json} Success-Response:
     *      HTTP/1.1 200 OK
     *      {
     *          "sessions": [
     *              {
     *                  "id": 90712,
     *                  "title": "testPool1",
     *                  "startDate": 1423670400000,
     *                  "endDate": 1423671300000,
     *                  "state": "UNDERWAY",
     *                  "meta": { },
     *                  "timestamp": 1418626800000
     *              }
     *          ],
     *          "userSessions": [
     *              90712
     *          ]
     *      }
     *
     * @apiError TargetNotFound No suitable sessions have been found in serviceTypeId passed
     * @apiErrorExample {json} TargetNotFound
     *      HTTP/1.1 404 Not Found
     *      {
     *          "status": "404",
     *          "title": "Invalid identifier"
     *      }
     *
     * @apiError UserNotLoggedIn User is not logged in.
     * @apiErrorExample {json} UserNotLoggedIn
     *      HTTP/1.1 401 Unauthorized
     *      {
     *          "status": "401",
     *          "title": "You are not logged in"
     *      }
     *
     *
     * @apiVersion 0.1.0
     */
    @ProfessionalAuth
    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public static Result getAssignedSessions() {
        int uid = Integer.parseInt(session("id"));

        // We list only the service types we offer, as we can't accept sessions of other types.
        List<Service> services = serviceService.getServicesOfUser(uid);
        List<ServiceType> acceptableServiceTypes =
                services.stream().map(o -> o.getServiceType()).collect(Collectors.toList());

        Map<Integer, Integer> poolsSize = sessionService.getPoolsSize();
        ArrayNode serviceTypes = new ArrayNode(JsonNodeFactory.instance);
        for (ServiceType serviceType: acceptableServiceTypes) {
            ObjectNode serviceTypeObject = (ObjectNode) Json.toJson(serviceType);
            int queue = poolsSize.containsKey(serviceType.getId()) ? poolsSize.get(serviceType.getId()) : 0;
            serviceTypeObject.put("waiting", queue);
            serviceTypes.add(serviceTypeObject);
        }

        // Get all our sessions.
        Set<Session.State> states = EnumSet.of(Session.State.UNDERWAY, Session.State.CLOSED);
        List<SessionUser> sessionUsers = sessionService.getUserSessionsByState(uid, states);

        ArrayNode sessions = new ArrayNode(JsonNodeFactory.instance);
        ObjectNode sessionsByServiceType = Json.newObject();

        for (SessionUser sessionUser: sessionUsers) {
            ServiceType serviceType = sessionUser.getSession().getServiceType();
            String serviceTypeId = Integer.toString(serviceType.getId());
            if (!sessionsByServiceType.has(serviceTypeId))
                sessionsByServiceType.put(serviceTypeId, new ArrayNode(JsonNodeFactory.instance));
            sessions.add(Json.toJson(sessionUser.getSession()));
            ((ArrayNode)sessionsByServiceType.get(serviceTypeId)).add(Json.toJson(sessionUser.getSession()));
            // If we can't offer this servicetype, show it anyway (we've been invited?)
            if (!acceptableServiceTypes.contains(serviceType)) {
                acceptableServiceTypes.add(serviceType);
                serviceTypes.add(Json.toJson(serviceType));
            }
        }

        ObjectNode result = Json.newObject()
                .putPOJO("serviceTypeSessions", sessionsByServiceType)
                .putPOJO("sessions", sessions)
                .putPOJO("servicetypes", serviceTypes);
        SignatureHelper.addSignatures(result, uid);
        return ok(result);
    }


    /**
     * @api {get} /user/session/pools Get pools size
     *
     * @apiGroup Session
     * @apiName GetPoolsSize
     * @apiDescription Returns the current queue size for each pool. Pools without queue are ignored.
     *
     * @apiSuccess {Object} pools Info about the assigned session.
     * @apiSuccessExample {json} Success-Response:
     *      HTTP/1.1 200 OK
     *      {
     *          "pools": {
     *              "90300": 1,
     *         }
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
    @ProfessionalAuth
    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public static Result getPoolsSize() {
        ObjectNode ret = Json.newObject();
        ret.put("pools", Json.toJson(sessionService.getPoolsSize()));
        return ok(ret);
    }

    /**
     * @api {get} /session/:sessionId Session
     * @apiGroup Session
     * @apiName GetSession
     * @apiDescription Collect info on a session and its participants.
     *
     * @apiParam {Integer} id Session unique ID.
     *
     * @apiSuccess {Object} session Info about the specified session.
     * @apiSuccess {Object} session.users Info about the participants on the session.
     * @apiSuccess {Object} session.users.me Info about the current user.
     * @apiSuccess {Object[]} session.users.professionals Info about other professionals in the session.
     * @apiSuccess {Object[]} session.users.clients Info about other clients in the session (for professionals only).
     *
     * @apiSuccessExample {json} Success-Response:
     *     HTTP/1.1 200 OK
     *     {
     *         "session": {
     *             "id": 90700,
     *             "title": "testSession1",
     *             "startDate": 1425312000000,
     *             "endDate": 1425312900000,
     *             "state": "PROGRAMMED",
     *             "meta": {},
     *             "timestamp": 1418626800000,
     *             "users": {
     *                 "me": {
     *                     "id": 90000,
     *                     "login": "testClient1",
     *                     "name": "Test",
     *                     "surname1": "Client",
     *                     "surname2": "User1",
     *                     "birthdate": "1987-12-24",
     *                     "avatar": null,
     *                     "nationalId": "12345678Z",
     *                     "gender": "FEMALE",
     *                     "meta": {},
     *                     "sessionMeta": {},
     *                     "report": null
     *                 },
     *                 "professionals": [
     *                     {
     *                         "id": 90005,
     *                         "login": "testProfessional1",
     *                         "name": "Test",
     *                         "surname1": "Professional",
     *                         "surname2": "User1",
     *                         "birthdate": "1969-12-31",
     *                         "avatar": "http://comps.canstockphoto.com/can-stock-photo_csp6253298.jpg",
     *                         "nationalId": "99999999Z",
     *                         "gender": "MALE",
     *                         "meta": {},
     *                         "sessionMeta": {},
     *                         "service": "Field1"
     *                     }
     *                 ]
     *             }
     *         }
     *     }
     *
     * @apiError SessionNotFound The <code>id</code> of the Session was not found.
     * @apiErrorExample {json} SessionNotFound
     *      HTTP/1.1 404 Not Found
     *      {
     *          "status": "404",
     *          "title": "Invalid identifier"
     *      }
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
    @ProfessionalAuth
    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public static Result getSession(int id) {
        int uid = Integer.parseInt(session("id"));
        Session session = sessionService.getById(id, uid);
        if (session == null) return status(404, JsonUtils.simpleError("404", "Invalid identifier"));

        List<SessionUser> sessionUsers = sessionService.getSessionUsers(id);
        List<Integer> participants = new ArrayList<>();
        List<User> users = new ArrayList<>();
        List<HistoryEntry> historyEntries = new ArrayList<>();
        ObjectNode userHistoryEntries = Json.newObject();

        for (SessionUser sessionUser: sessionUsers) {
            User u = sessionUser.getUser();
            if (u.getId() == uid) {
                sessionUser.setViewedDate(new Date());
            }
            participants.add(sessionUser.getId());
            users.add(u);
            if (u.getType().equals("client")) {
                List<HistoryEntry> userHistory = historyService.getHistory(u.getId());
                List<Integer> userHistoryIds = userHistory.stream().map(o -> o.getId()).collect(Collectors.toList());
                historyEntries.addAll(userHistory);
                userHistoryEntries.put(Integer.toString(u.getId()), Json.toJson(userHistoryIds));
            }
        }

        return ok(Json.newObject()
            .putPOJO("sessions", new ArrayNode(JsonNodeFactory.instance).add(Json.toJson(session)))
            .putPOJO("users", Json.toJson(users))
            .putPOJO("sessionusers", Json.toJson(sessionUsers))
            .putPOJO("participants", Json.newObject().putPOJO(Integer.toString(id), Json.toJson(participants)))
            .putPOJO("historyentries", Json.toJson(historyEntries))
            .putPOJO("userHistoryEntries", userHistoryEntries));
    }
}

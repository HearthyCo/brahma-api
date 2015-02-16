package gl.glue.brahma.controllers.client;

import actions.BasicAuth;
import actions.ClientAuth;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gl.glue.brahma.model.session.Session;
import gl.glue.brahma.model.sessionuser.SessionUser;
import gl.glue.brahma.service.SessionService;
import gl.glue.brahma.util.JsonUtils;
import gl.glue.brahma.util.ModelSecurity;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.Date;
import java.util.List;

public class SessionController extends Controller {

    private static SessionService sessionService = new SessionService();

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
    @ClientAuth
    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public static Result getSession(int id) {
        int uid = Integer.parseInt(session("id"));

        // Get session with id param
        ObjectNode result = sessionService.getSession(id, uid);
        if (result == null) return status(404, JsonUtils.simpleError("404", "Invalid identifier"));

        return ok(result);
    }


    /**
     * @api {get} /user/sessions/:state Sessions by state
     * @apiGroup Session
     * @apiName GetUserSessionsByState
     * @apiDescription Return all the Sessions in which the current user participates, that are on the given state.
     *
     * @apiParam {String} state The session state. One of: `programmed`, `underway` or `closed`.
     *
     * @apiSuccess {Object[]} sessions Info about the matching sessions.
     * @apiSuccess {Boolean} session.isNew Whether the session contains changes not yet seen by the user.
     *
     * @apiSuccessExample {json} Success-Response:
     *     HTTP/1.1 200 OK
     *     {
     *         "sessions": [
     *             {
     *                 "id": 90700,
     *                 "title": "testSession1",
     *                 "startDate": 1425312000000,
     *                 "endDate": 1425312900000,
     *                 "state": "PROGRAMMED",
     *                 "meta": {},
     *                 "timestamp": 1418626800000,
     *                 "isNew": false
     *             },
     *             {
     *                 "id": 90704,
     *                 "title": "testSession5",
     *                 "startDate": 1425571200000,
     *                 "endDate": 1426176900000,
     *                 "state": "PROGRAMMED",
     *                 "meta": {},
     *                 "timestamp": 1418626800000,
     *                 "isNew": true
     *             }
     *         ]
     *     }
     *
     * @apiError StateNotFound The <code>state</code> contains a unknown value.
     * @apiErrorExample {json} StateNotFound
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
    @ClientAuth
    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public static Result getState(String state) {
        int uid = Integer.parseInt(session("id"));

        List<SessionUser> sessionUsers = sessionService.getState(state, uid);
        if (sessionUsers == null) return status(404, JsonUtils.simpleError("404", "Invalid identifier"));

        ArrayNode sessions = new ArrayNode(JsonNodeFactory.instance);
        for(SessionUser sessionUser : sessionUsers) {
            Session session = sessionUser.getSession();

            boolean isNew = true;
            if(sessionUser.getViewedDate() != null) {
                isNew = session.getTimestamp().after(sessionUser.getViewedDate());
            }

            ObjectNode sessionObject = (ObjectNode) Json.toJson(session);
            sessionObject.put("isNew", isNew);

            sessions.add(sessionObject);
        }

        ObjectNode result = Json.newObject();
        result.put("sessions", sessions);
        return ok(result);
    }

    /**
     * @api {post} /user/session New sessions
     *
     * @apiGroup Session
     * @apiName NewSession
     * @apiDescription Request new session
     *
     * @apiParam {String}   state       The session state. One of: `programmed`, `underway` or `requested`.
     * @apiParam {Integer}  serviceType Service type id.
     * @apiParam {Long}     date        Timestamp date.
     * @apiParamExample {json} Request-Example
     *      {
     *          "service": 1,
     *          "state": "PROGRAMMED",
     *          "startDate": 1423699595548
     *      }
     *
     * @apiSuccess {Object[]} session Info about the session created.
     * @apiSuccessExample {json} Success-Response:
     *      HTTP/1.1 200 OK
     *      {
     *         "session": {
     *             "id": 90700,
     *             "title": "Consulta 11-02-2015",
     *             "startDate": 1425312000000,
     *             "endDate": null,
     *             "state": "PROGRAMMED",
     *             "meta": {},
     *             "timestamp": 1418626800000,
     *             "isNew": true
     *         }
     *     }
     *
     * @apiError StateNotFound The <code>state</code> contains a unknown value.
     * @apiErrorExample {json} StateNotFound
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
     * @apiError MissingRequiredField Missing required field
     * @apiErrorExample {json} MissingRequiredField
     *      HTTP/1.1 400 BadRequest
     *      {
     *          "status": "400",
     *          "title": "You are not logged in"
     *      }
     *
     * @apiVersion 0.1.0
     */
    @ClientAuth
    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public static Result requestSession() {
        int uid = Integer.parseInt(session("id"));
        JsonNode json = request().body().asJson();

        ObjectNode result = JsonUtils.checkRequiredFields(json, ModelSecurity.SESSION_REQUIRED_FIELDS);
        if (result != null) return badRequest(result);

        int serviceType = json.findPath("service").asInt();
        Date startDate = new Date(json.findPath("startDate").asLong());

        Session.State state;
        try {
            state = Session.State.valueOf(json.findPath("state").asText());
        } catch (IllegalArgumentException e) {
            return status(400, JsonUtils.invalidRequiredField("Session state"));
        }

        Session session;
        if (state == Session.State.PROGRAMMED) {
            Date now = new Date();
            if (startDate == null || now.after(startDate)) return status(400, JsonUtils.invalidRequiredField("Date"));

            session = sessionService.requestSession(uid, serviceType, state, startDate);
        } else {
            session = sessionService.requestSession(uid, serviceType, state);
        }

        if (session == null) return status(404, JsonUtils.simpleError("404", "Invalid user identifier"));

        ObjectNode sessionRet = (ObjectNode) Json.toJson(session);
        sessionRet.put("isNew", true);

        result = Json.newObject();
        result.put("session", sessionRet);
        return ok(result);
    }


    /**
     * @api {post} /user/session/assignPool Assign session from pool
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
    @ClientAuth
    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public static Result assignSessionFromPool() {
        int uid = Integer.parseInt(session("id"));

        JsonNode json = request().body().asJson();
        if (!json.has("serviceType")) {
            return badRequest(JsonUtils.missingRequiredField("serviceType"));
        }
        int type = json.get("serviceType").asInt();

        Session session = sessionService.assignSessionFromPool(uid, type);
        if (session == null) {
            return notFound(JsonUtils.simpleError("404", "Couldn't assign any session"));
        }

        ObjectNode res = Json.newObject();
        res.put("session", Json.toJson(session));
        return ok(res);
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
    @ClientAuth
    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public static Result getPoolsSize() {
        ObjectNode ret = Json.newObject();
        ret.put("pools", Json.toJson(sessionService.getPoolsSize()));
        return ok(ret);
    }

}

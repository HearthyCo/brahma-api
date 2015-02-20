package gl.glue.brahma.controllers.professional;


import actions.ClientAuth;
import actions.ProfessionalAuth;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gl.glue.brahma.model.session.Session;
import gl.glue.brahma.service.SessionService;
import gl.glue.brahma.util.JsonUtils;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

public class SessionController extends Controller {

    private static SessionService sessionService = new SessionService();

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
    @ProfessionalAuth
    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public static Result getPoolsSize() {
        ObjectNode ret = Json.newObject();
        ret.put("pools", Json.toJson(sessionService.getPoolsSize()));
        return ok(ret);
    }
}

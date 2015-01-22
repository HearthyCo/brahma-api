package gl.glue.brahma.controllers;

import actions.BasicAuth;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gl.glue.brahma.model.session.Session;
import gl.glue.brahma.model.sessionuser.SessionUser;
import gl.glue.brahma.service.SessionService;
import gl.glue.brahma.util.JsonUtils;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.List;

public class SessionController extends Controller {

    private static SessionService sessionService = new SessionService();

    /**
     * @api {get} /session/:sessionId Session
     * @apiName GetSession
     * @apiGroup Session
     *
     * @apiParam {Integer} id Session unique ID.
     *
     * @apiParamExample {json} Request-Example:
     *     {
     *       "id": 4711
     *     }
     *
     * @apiVersion 0.1.0
     */
    @BasicAuth
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
     * @apiName GetUserSessionsByState
     * @apiGroup Session
     *
     * @apiParam {String} state Sessions state: `programmed`, `underway`, `closed`.
     *
     * @apiParamExample {json} Request-Example:
     *     {
     *       "state": "underway"
     *     }
     * @apiVersion 0.1.0
     */
    @BasicAuth
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
}

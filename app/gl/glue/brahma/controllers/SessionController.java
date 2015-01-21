package gl.glue.brahma.controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import gl.glue.brahma.service.SessionService;
import gl.glue.brahma.util.JsonUtils;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.ArrayList;

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
    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public static Result getSession(int id) {

        // Check if login
        String login = session("login");
        if(login == null) return unauthorized("You are not logged in");

        // Get session with id param
        ObjectNode result = sessionService.getSession(id, login);
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
    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public static Result getState(String state) {

        String login = session("login");
        if(login == null) return unauthorized("You are not logged in");

        ArrayList<ObjectNode> sessions = sessionService.getState(state, login);
        if (sessions == null) return status(404, JsonUtils.simpleError("404", "Invalid identifier"));

        ObjectNode result = Json.newObject();
        result.put("sessions", Json.toJson(sessions));

        return ok(result);
    }
}

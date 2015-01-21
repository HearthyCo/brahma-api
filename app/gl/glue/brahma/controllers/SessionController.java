package gl.glue.brahma.controllers;

import actions.BasicAuth;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

    @BasicAuth
    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public static Result getSession(int id) {

        // Check if login
        if(session("id") == null) return unauthorized("You are not logged in");
        int uid = Integer.parseInt(session("id"));

        // Get session with id param
        ObjectNode result = sessionService.getSession(id, uid);
        if (result == null) return status(404, JsonUtils.simpleError("404", "Invalid identifier"));

        return ok(result);
    }

    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public static Result getState(String state) {

        if(session("id") == null) return unauthorized("You are not logged in");
        int uid = Integer.parseInt(session("id"));

        List<ObjectNode> sessions = sessionService.getState(state, uid);
        if (sessions == null) return status(404, JsonUtils.simpleError("404", "Invalid identifier"));

        ObjectNode result = Json.newObject();
        result.put("sessions", Json.toJson(sessions));

        return ok(result);
    }
}
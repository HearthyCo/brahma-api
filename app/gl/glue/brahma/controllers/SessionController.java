package gl.glue.brahma.controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import gl.glue.brahma.model.session.Session;
import gl.glue.brahma.service.SessionService;
import gl.glue.brahma.util.JsonUtils;
import play.Logger;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.List;

public class SessionController extends Controller {

    private static SessionService sessionService = new SessionService();

    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public static Result getSession(int id) {
        String login = session("login");
        if(login == null) {
            return unauthorized("You are not logged in");
        }

        Session session = sessionService.getSession(id, login);
        if (session == null) {
            return status(404, JsonUtils.simpleError("404", "Invalid identifier"));
        }

        ObjectNode result = Json.newObject();
        result.put("session", Json.toJson(session));
        return ok(result);
    }

    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public static Result getState(String state) {

        String login = session("login");
        Logger.info("STATE " + state + " FOR " + login + " -");
        if(login == null) {
            return unauthorized("You are not logged in");
        }

        List<Session> sessions = sessionService.getState(state, login);
        if (sessions == null) {
            return status(404, JsonUtils.simpleError("404", "Invalid identifier"));
        }

        ObjectNode result = Json.newObject();
        result.put("state", state);
        result.put("count", sessions.size());
        result.put("sessions", Json.toJson(sessions));
        return ok(result);
    }
}

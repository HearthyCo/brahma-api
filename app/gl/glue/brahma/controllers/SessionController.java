package gl.glue.brahma.controllers;

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

    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public static Result get(int id) {
        String login = session("login");
        if(login == null) {
            return unauthorized("You are not logged in");
        }

        Session session = sessionService.get(id, login);
        if (session == null) {
            return status(401, JsonUtils.simpleError("401", "Invalid identifier"));
        }

        ObjectNode result = Json.newObject();
        result.put("session", Json.toJson(session));
        return ok(result);
    }
}

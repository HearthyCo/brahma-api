package gl.glue.brahma.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gl.glue.brahma.service.SessionService;
import gl.glue.brahma.util.JsonUtils;
import play.mvc.Controller;
import play.mvc.Result;

public class SessionController extends Controller {

    private static SessionService sessionService = new SessionService();

    public static Result get(int session) {
        JsonNode json = request().body().asJson();
        ObjectNode result = JsonUtils.checkRequiredFields(json, "session.id");
        return result;
    }
}

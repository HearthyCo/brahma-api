package gl.glue.brahma.controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import gl.glue.brahma.service.HomeService;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

public class HomeController extends Controller {

    private static HomeService homeService = new HomeService();

    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public static Result get() {
        // Check if login
        String login = session("login");
        if(login == null) return unauthorized("You are not logged in");

        // Get session with login
        ObjectNode sessions = homeService.getSessions(login);

        ObjectNode result = Json.newObject();
        result.put("sessions", sessions);

        return ok(result);
    }

}

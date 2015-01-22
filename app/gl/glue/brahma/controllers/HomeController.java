package gl.glue.brahma.controllers;

import actions.BasicAuth;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gl.glue.brahma.service.HomeService;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

public class HomeController extends Controller {

    private static HomeService homeService = new HomeService();


    /**
     * @api {get} /user/home Homepage
     * @apiName Home
     * @apiGroup User
     *
     * @apiVersion 0.1.0
     */
    @BasicAuth
    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public static Result get() {
        int uid = Integer.parseInt(session("id"));

        // Get session with login
        ObjectNode sessions = homeService.getSessions(uid);

        ObjectNode result = Json.newObject();
        result.put("sessions", sessions);



        return ok(result);
    }
}

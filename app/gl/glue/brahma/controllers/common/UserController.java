package gl.glue.brahma.controllers.common;

import actions.BasicAuth;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gl.glue.brahma.model.user.User;
import gl.glue.brahma.service.UserService;
import gl.glue.brahma.util.JsonUtils;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

public class UserController extends Controller {

    private static UserService userService = new UserService();

    /**
     * @api {post} /user/logout Logout
     *
     * @apiGroup User
     * @apiName Logout
     * @apiDescription Destroy user session.
     *
     * @apiSuccessExample {json} Success-Response
     *      HTTP/1.1 200 OK
     *      {
     *      }
     *
     * @apiVersion 0.1.0
     */
    @Transactional
    public static Result logout() {
        session().clear();
        return ok(Json.newObject());
    }

    @BasicAuth
    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public static Result confirmMail() {
        JsonNode json = request().body().asJson();
        ObjectNode result = JsonUtils.checkRequiredFields(json, "hash");
        if (result != null) return badRequest(result);

        int uid = Integer.parseInt(session("id"));
        if (userService.confirmMail(uid, json.get("hash").asText())) {
            return ok(Json.newObject());
        } else {
            return status(403, JsonUtils.simpleError("403", "Hash validation failed."));
        }
    }

    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public static Result requestPasswordChange() {
        JsonNode json = request().body().asJson();
        ObjectNode result = JsonUtils.checkRequiredFields(json, "email");
        if (result != null) return badRequest(result);

        User user = userService.requestPasswordChange(json.get("email").asText());
        if (user != null) {
            return ok(Json.newObject());
        } else {
            return status(404, JsonUtils.simpleError("404", "User not found."));
        }
    }

    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public static Result confirmPasswordChange() {
        JsonNode json = request().body().asJson();
        ObjectNode result = JsonUtils.checkRequiredFields(json, "email", "hash", "newPassword");
        if (result != null) return badRequest(result);

        String email = json.get("email").asText();
        String hash = json.get("hash").asText();
        String newPassword = json.get("newPassword").asText();
        if (userService.confirmPasswordChange(email, hash, newPassword)) {
            // Should this also perform a login?
            return ok(Json.newObject());
        } else {
            return status(403, JsonUtils.simpleError("403", "Hash validation failed."));
        }
    }

}
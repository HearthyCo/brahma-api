package gl.glue.brahma.controllers.common;

import actions.BasicAuth;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gl.glue.brahma.model.user.User;
import gl.glue.brahma.service.UserService;
import gl.glue.brahma.util.JsonUtils;
import gl.glue.brahma.util.SignatureHelper;
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

    /**
     * @api {get} /user/me Get Me
     *
     * @apiGroup User
     * @apiName Get Me
     * @apiDescription Get the current logged in user.
     *
     * @apiSuccess {Array}  users   Contains all user fields after login
     * @apiSuccess {Array}  sign    Contains all signed user content index (sessions, id)
     * @apiSuccessExample {json} Success-Response
     *      HTTP/1.1 200 OK
     *      {
     *          "users": [
     *              {
     *                  "id": 90005,
     *                  "login": null,
     *                  "email": "testuser1@glue.gl",
     *                  "name": "Test",
     *                  "surname1": "User",
     *                  "surname2": "User1",
     *                  "birthdate": "1969-12-31",
     *                  "avatar": "http://...",
     *                  "nationalId": "12345678A",
     *                  "gender": "OTHER",
     *                  "state": "UNCONFIRMED",
     *                  "balance": 0,
     *                  "type": "user"
     *                  "locked": false,
     *                  "confirmed": false,
     *                  "banned": false,
     *                  "meta": {},
     *              }
     *          ],
     *          "sign": [
     *              {
     *                  "id": "sessions",
     *                  "signature": "jBFTvM5669uJ9eLbN8CUhyAUTmgkjUpXn1GLXqOtR5Q=1425987517615",
     *                  "value": [ 90700, 90712 ]
     *              },
     *              {
     *                  "id": "userId",
     *                  "signature": "oG8urM4fQFc4ma2fJ58TtAC/lO9CUwDa73goXytm1NA=1425987517619",
     *                  "value": 90005
     *              }
     *          ]
     *      }
     *
     * @apiVersion 0.1.0
     *
     * @apiError {Object} UserNotLoggedIn User is not logged in.
     * @apiErrorExample {json} UserNotLoggedIn
     *      HTTP/1.1 401 Unauthorized
     *      {
     *          "status": "401",
     *          "title": "You are not logged in"
     *      }
     *
     * @apiError {Object} LockedUser User is not logged in.
     * @apiErrorExample {json} LockedUser
     *      HTTP/1.1 403 Locked
     *      {
     *          "status": "403",
     *          "title": "Banned or removed user"
     *      }
     */
    @BasicAuth
    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public static Result getMe() {
        User user = userService.getById(Integer.parseInt(session("id")));

        if (user.isLocked()) return status(403, JsonUtils.simpleError("403", "Banned or removed user"));

        ObjectNode result = Json.newObject();
        ArrayNode users = new ArrayNode(JsonNodeFactory.instance);
        users.add(Json.toJson(user));
        result.put("users", users);
        SignatureHelper.addSignatures(result, user.getId());
        return ok(result);
    }

    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public static Result confirmMail() {
        JsonNode json = request().body().asJson();
        ObjectNode result = JsonUtils.checkRequiredFields(json, "userId", "hash");
        if (result != null) return badRequest(result);

        if (userService.confirmMail(json.get("userId").asInt(), json.get("hash").asText())) {
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
        ObjectNode result = JsonUtils.checkRequiredFields(json, "userId", "hash", "newPassword");
        if (result != null) return badRequest(result);

        int uid = json.get("userId").asInt();
        String hash = json.get("hash").asText();
        String newPassword = json.get("newPassword").asText();
        if (userService.confirmPasswordChange(uid, hash, newPassword)) {
            // Should this also perform a login?
            return ok(Json.newObject());
        } else {
            return status(403, JsonUtils.simpleError("403", "Hash validation failed."));
        }
    }
}
package gl.glue.brahma.controllers.common;

import actions.BasicAuth;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.wordnik.swagger.annotations.*;
import gl.glue.brahma.model.user.User;
import gl.glue.brahma.service.UserService;
import gl.glue.brahma.util.JsonUtils;
import gl.glue.brahma.util.SignatureHelper;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

@Api(value = "/common", description = "User functions")
public class UserController extends Controller {

    private static UserService userService = new UserService();

    @ApiOperation(nickname = "logout", value = "User logout",
            notes = "Destroy user session.",
            httpMethod = "POST")
    @Transactional
    public static Result logout() {
        session().clear();
        return ok(Json.newObject());
    }

    @ApiOperation(nickname = "getMe", value = "Get Me", notes = "Get the current user logged", httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 401, message = "User is not logged in.") })
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

    @ApiOperation(nickname = "confirmMail", value = "Confirm mail user",
            notes = "Allow user (registered before) can confirm account.",
            httpMethod = "POST")
    @ApiImplicitParams(value= {
            @ApiImplicitParam(name = "body", defaultValue = "{\n" +
                    "  \"userId\": \"testClient100@glue.gl\",\n" +
                    "  \"hash\": \"\"\n" +
                    "}", value="Object with post params", required = true, dataType = "Object", paramType = "body")})
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Params has not a required field."),
            @ApiResponse(code = 403, message = "Unauthorized type user.")})
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

    @ApiOperation(nickname = "requestPasswordChange", value = "Change password",
            notes = "Allow user (registered before) can change password account.",
            httpMethod = "POST")
    @ApiImplicitParams(value= {
            @ApiImplicitParam(name = "body", defaultValue = "{\n" +
                    "  \"email\": \"testClient1@glue.gl\",\n" +
                    "}", value="Object with post params", required = true, dataType = "Object", paramType = "body")})
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Params has not a required field."),
            @ApiResponse(code = 404, message = "User not found.")})
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

    @ApiOperation(nickname = "confirmPasswordChange", value = "Confirm password",
            notes = "Allow user (registered before) confirm new password account (requested before).",
            httpMethod = "POST")
    @ApiImplicitParams(value= {
            @ApiImplicitParam(name = "body", defaultValue = "{\n" +
                    "  \"userId\": \"testClient1@glue.gl\",\n" +
                    "  \"hash\": \"\"\n" +
                    "  \"newPassword\": \"newDummyPassword@glue.gl\"\n" +
                    "}", value="Object with post params", required = true, dataType = "Object", paramType = "body")})
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Params has not a required field."),
            @ApiResponse(code = 403, message = "Hash validation failed.")})
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
package gl.glue.brahma.controllers.admin;

import actions.AdminAuth;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.wordnik.swagger.annotations.*;
import gl.glue.brahma.model.user.Admin;
import gl.glue.brahma.model.user.User;
import gl.glue.brahma.service.UserService;
import gl.glue.brahma.util.JsonUtils;
import gl.glue.brahma.util.ModelSecurity;
import gl.glue.brahma.util.SignatureHelper;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.*;

@Api(value = "/admin", description = "Admin functions")
public class UserController extends Controller {

    private static UserService userService = new UserService();

    @ApiOperation(nickname = "login", value = "User admin login",
            notes = "Allow admin user (registered before) can be identified to access his private information.",
            httpMethod = "POST")
    @ApiImplicitParams(value= {
            @ApiImplicitParam(name = "body", defaultValue = "{\n" +
                    "  \"email\": \"testadmin1@glue.gl\",\n" +
                    "  \"password\": \"testAdmin1@glue.gl\"\n" +
                    "}", value="Object with post params", required = true, dataType = "Object", paramType = "body")})
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Params has not a required field."),
            @ApiResponse(code = 401, message = "Email or password is wrong."),
            @ApiResponse(code = 403, message = "Unauthorized type user"),
            @ApiResponse(code = 403, message = "Banned or removed user.")})
    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public static Result login() {
        JsonNode json = request().body().asJson();

        ObjectNode result = JsonUtils.checkRequiredFields(json, "email", "password");
        if (result != null) return badRequest(result);

        String login = json.findPath("email").textValue();
        String pass = json.findPath("password").textValue();

        User user = userService.login(login, pass);
        if (user == null) return status(401, JsonUtils.simpleError("401", "Invalid username or password."));
        if (!(user instanceof Admin)) return status(403, JsonUtils.simpleError("403", "Unauthorized type user"));
        if (!user.canLogin()) return status(403, JsonUtils.simpleError("403", "Banned user"));

        session().clear();
        session("id", Integer.toString(user.getId()));
        session("role", user.getUserType());

        result = Json.newObject();
        result.put("users", new ArrayNode(JsonNodeFactory.instance).add(Json.toJson(user)));
        SignatureHelper.addSignatures(result, user.getId());
        return ok(result);
    }

    @ApiOperation(nickname = "updateProfile", value = "User admin update profile",
            notes = "Allow admin user (registered before) can to update fields profile.",
            httpMethod = "POST")
    @ApiImplicitParams(value= {
            @ApiImplicitParam(name = "body", defaultValue = "{\n" +
                    "  \"surname1\": \"Dummy\",\n" +
                    "  \"nationalId\": \"00000000C\"\n" +
                    "}", value="Object with post params", required = true, dataType = "Object", paramType = "body")})
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Params has not a required field."),
            @ApiResponse(code = 401, message = "User is not logged in."),
            @ApiResponse(code = 403, message = "Banned or removed user.")})
    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public static Result updateProfile() {
        JsonNode json = request().body().asJson();
        json = JsonUtils.cleanFields((ObjectNode) json, ModelSecurity.USER_PROFILE_MODIFIABLE_FIELDS);

        Iterator<String> fields = json.fieldNames();
        List<String> availableFields = new ArrayList<>();
        while(fields.hasNext()) availableFields.add(fields.next());

        // Parse the incoming data
        Admin admin;
        try {
            admin = Json.fromJson(json, Admin.class);
        } catch (RuntimeException e) {
            return status(400, JsonUtils.handleDeserializeException(e, "user"));
        }

        User user = userService.getById(Integer.parseInt(session("id")));

        if (user.isLocked()) return status(403, JsonUtils.simpleError("403", "Banned or removed user"));

        user.merge(admin, availableFields);

        ObjectNode result = Json.newObject();
        result.put("users", new ArrayNode(JsonNodeFactory.instance).add(Json.toJson(user)));
        SignatureHelper.addSignatures(result, user.getId());
        return ok(result);
    }

    @ApiOperation(nickname = "getMe", value = "Get Me", notes = "Get the current admin logged", httpMethod = "GET")
    @ApiResponses(value = { @ApiResponse(code = 401, message = "User is not logged in.") })
    @AdminAuth
    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public static Result getMe() {
        return gl.glue.brahma.controllers.common.UserController.getMe();
    }
}

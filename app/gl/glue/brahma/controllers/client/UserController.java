package gl.glue.brahma.controllers.client;

import actions.ClientAuth;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.wordnik.swagger.annotations.*;
import gl.glue.brahma.model.user.Client;
import gl.glue.brahma.model.user.User;
import gl.glue.brahma.service.UserService;
import gl.glue.brahma.util.JsonUtils;
import gl.glue.brahma.util.ModelSecurity;
import gl.glue.brahma.util.SignatureHelper;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import javax.persistence.PersistenceException;
import java.util.*;

@Api(value = "/client", description = "User client functions")
public class UserController extends Controller {

    private static UserService userService = new UserService();

    @ApiOperation(nickname = "login", value = "User client login",
            notes = "Allow client user (registered before) can be identified to access his private information.",
            httpMethod = "POST")
    @ApiImplicitParams(value= {
            @ApiImplicitParam(name = "body", defaultValue = "{\n" +
                    "  \"email\": \"testclient1@glue.gl\",\n" +
                    "  \"password\": \"testClient1@glue.gl\"\n" +
                    "}", value="Object with post params", required = true, dataType = "Object", paramType = "body")})
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Params has not a required field."),
            @ApiResponse(code = 401, message = "Email or password is wrong."),
            @ApiResponse(code = 403, message = "Unauthorized type user.")})
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
        if (!(user instanceof Client)) return status(403, JsonUtils.simpleError("403", "Unauthorized type user"));
        if (!user.canLogin()) return status(423, JsonUtils.simpleError("423", "Locked user"));

        session().clear();
        session("id", Integer.toString(user.getId()));
        session("role", user.getUserType());

        result = Json.newObject();
        result.put("users", new ArrayNode(JsonNodeFactory.instance).add(Json.toJson(user)));
        SignatureHelper.addSignatures(result, user.getId());
        return ok(result);
    }

    @ApiOperation(nickname = "login", value = "Client register",
            notes = "Allow client can to register in service.",
            httpMethod = "POST")
    @ApiImplicitParams(value= {
            @ApiImplicitParam(name = "body", defaultValue = "{\n" +
                    "  \"email\": \"testclient100@glue.gl\",\n" +
                    "  \"password\": \"testClient100@glue.gl\"\n" +
                    "}", value="Object with post params", required = true, dataType = "Object", paramType = "body")})
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Params has not a required field."),
            @ApiResponse(code = 401, message = "Email or password is wrong."),
            @ApiResponse(code = 403, message = "Unauthorized type user."),
            @ApiResponse(code = 409, message = "Email already in use.")})
    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public static Result register() {

        JsonNode json = request().body().asJson();
        ObjectNode result = JsonUtils.checkRequiredFields(json, ModelSecurity.USER_REQUIRED_FIELDS);
        if (result != null) return badRequest(result);

        // Parse the incoming data
        Client client;
        try {
            json = JsonUtils.cleanFields((ObjectNode)json, ModelSecurity.USER_MODIFIABLE_FIELDS);
            client = Json.fromJson(json, Client.class);
        } catch (RuntimeException e) {
            return status(400, JsonUtils.handleDeserializeException(e, "user"));
        }

        // Quick checks
        if (client.getGender() == null) client.setGender(User.Gender.OTHER); // TODO: Better value here?

        // Register the user
        User user;
        try {
            user = userService.register(client);
        } catch (PersistenceException e) {
            return status(409, JsonUtils.simpleError("409", "Email already in use."));
        }

        // Also log him in
        session().clear();
        session("id", Integer.toString(user.getId()));
        session("role", user.getUserType());

        result = Json.newObject();
        result.put("users", new ArrayNode(JsonNodeFactory.instance).add(Json.toJson(user)));
        SignatureHelper.addSignatures(result, user.getId());
        return ok(result);
    }

    @ApiOperation(nickname = "updateProfile", value = "User client update profile",
            notes = "Allow client user (registered before) can to update fields profile.",
            httpMethod = "POST")
    @ApiImplicitParams(value= {
            @ApiImplicitParam(name = "body", defaultValue = "{\n" +
                    "  \"surname1\": \"Dummy\",\n" +
                    "  \"nationalId\": \"00000000C\"\n" +
                    "}", value="Object with post params", required = true, dataType = "Object", paramType = "body")})
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Params has not a required field."),
            @ApiResponse(code = 401, message = "User is not logged in."),
            @ApiResponse(code = 403, message = "Unauthorized."),
            @ApiResponse(code = 403, message = "Banned or removed user.") })
    @ClientAuth
    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public static Result updateProfile() {

        JsonNode json = request().body().asJson();
        json = JsonUtils.cleanFields((ObjectNode)json, ModelSecurity.USER_PROFILE_MODIFIABLE_FIELDS);

        Iterator<String> fields = json.fieldNames();
        List<String> availableFields = new ArrayList<>();
        while(fields.hasNext()) availableFields.add(fields.next());

        // Parse the incoming data
        Client client;
        try {
            client = Json.fromJson(json, Client.class);
        } catch (RuntimeException e) {
            return status(400, JsonUtils.handleDeserializeException(e, "user"));
        }

        User user = userService.getById(Integer.parseInt(session("id")));

        if (user.isLocked()) return status(423, JsonUtils.simpleError("403", "Banned or removed user"));

        user.merge(client, availableFields);

        ObjectNode result = Json.newObject();
        result.put("users", new ArrayNode(JsonNodeFactory.instance).add(Json.toJson(user)));

        return ok(result);
    }

    @ApiOperation(nickname = "getMe", value = "Get Me", notes = "Get the current client logged", httpMethod = "GET")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "User is not logged in."),
            @ApiResponse(code = 403, message = "Unauthorized") })
    @ClientAuth
    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public static Result getMe() {
        return gl.glue.brahma.controllers.common.UserController.getMe();
    }


    @ApiOperation(nickname = "setAvatar", value = "Set Avatar",
            notes = "Set avatar for the currently logged in client", httpMethod = "POST")
    @ApiImplicitParams(value= {
            @ApiImplicitParam(name = "upload", required = true, dataType = "File", paramType = "file")})
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Missing upload file."),
            @ApiResponse(code = 401, message = "User is not logged in."),
            @ApiResponse(code = 403, message = "Unauthorized") })
    @ClientAuth
    @Transactional
    public static Result setAvatar() {
        Http.MultipartFormData body = request().body().asMultipartFormData();
        if (body == null) return status(400, JsonUtils.simpleError("400", "Expected multipart/form-data enctype"));
        Http.MultipartFormData.FilePart uploadFilePart = body.getFile("upload");
        if (uploadFilePart == null) {
            return status(400, JsonUtils.simpleError("400", "Missing upload file"));
        }

        int uid = Integer.parseInt(session("id"));
        User user = userService.setAvatar(uid, uploadFilePart.getFile());

        return ok(Json.newObject().putPOJO("users", new ArrayNode(JsonNodeFactory.instance).add(Json.toJson(user))));
    }

}
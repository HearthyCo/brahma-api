package gl.glue.brahma.controllers.admin;


import actions.AdminAuth;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.wordnik.swagger.annotations.*;
import gl.glue.brahma.model.user.Professional;
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

import javax.persistence.PersistenceException;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@Api(value = "/users", description = "Admin Users functions")
public class UsersController extends Controller {

    private static UserService userService = new UserService();

    @ApiOperation(nickname = "createProfessional", value = "Create professional",
            notes = "Allow admin to create a professional users.",
            httpMethod = "POST")
    @ApiImplicitParams(value= {
            @ApiImplicitParam(name = "body", defaultValue = "{\n" +
                    "  \"email\": \"testclient100@glue.gl\",\n" +
                    "  \"password\": \"testClient100@glue.gl\"\n" +
                    "}", value="Object with post params", required = true, dataType = "Object", paramType = "body")})
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Params has not a required field."),
            @ApiResponse(code = 401, message = "Email or password is wrong."),
            @ApiResponse(code = 403, message = "Unauthorized."),
            @ApiResponse(code = 409, message = "Email already in use.")})
    @AdminAuth
    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public static Result createProfessional() {
        JsonNode json = request().body().asJson();
        ObjectNode result = JsonUtils.checkRequiredFields(json, ModelSecurity.USER_REQUIRED_FIELDS);
        if (result != null) return badRequest(result);

        // Parse the incoming data
        Professional professional;
        try {
            json = JsonUtils.cleanFields((ObjectNode)json, ModelSecurity.USER_MODIFIABLE_FIELDS);
            professional = Json.fromJson(json, Professional.class);
        } catch (RuntimeException e) {
            return status(400, JsonUtils.handleDeserializeException(e, "user"));
        }

        // Quick checks
        if (professional.getGender() == null) professional.setGender(User.Gender.OTHER); // TODO: Better value here?

        // Register the user
        User user;
        try {
            user = userService.register(professional);
        } catch (PersistenceException e) {
            return status(409, JsonUtils.simpleError("409", "Email already in use."));
        }

        result = Json.newObject();
        result.putPOJO("users", new ArrayNode(JsonNodeFactory.instance).add(Json.toJson(user)));
        SignatureHelper.addSignatures(result, user.getId());
        return ok(result);
    }

    @ApiOperation(nickname = "readProfessional", value = "Read a professional",
            notes = "Allow admin to read an professional user.",
            httpMethod = "GET")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Params has not a required field."),
            @ApiResponse(code = 401, message = "Email or password is wrong."),
            @ApiResponse(code = 403, message = "Unauthorized."),
            @ApiResponse(code = 403, message = "Unauthorized type user."),
            @ApiResponse(code = 404, message = "Invalid identifier.")})
    @AdminAuth
    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public static Result readProfessional(
            @ApiParam(value = "Professional id", required = true) @PathParam("id") int uid) {
        User user = userService.getById(uid);

        if (user == null) return status(404, JsonUtils.simpleError("404", "Invalid identifier"));

        if (!(user instanceof Professional)) return status(403, JsonUtils.simpleError("403", "Unauthorized type user"));

        ArrayNode users = new ArrayNode(JsonNodeFactory.instance);
        users.add(Json.toJson(user));

        ObjectNode result = Json.newObject();
        result.putPOJO("users", users);
        SignatureHelper.addSignatures(result, user.getId());
        return ok(result);
    }

    @ApiOperation(nickname = "readProfessionals", value = "Read all professional",
            notes = "Allow admin to read all professional users.",
            httpMethod = "GET")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Params has not a required field."),
            @ApiResponse(code = 401, message = "Email or password is wrong."),
            @ApiResponse(code = 403, message = "Unauthorized.")})
    @AdminAuth
    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public static Result readProfessionals() {
        List<Professional> professionalUsers = userService.getByType(Professional.class);

        List<Integer> professionalUsersIds = professionalUsers.stream().map(o->o.getId()).collect(Collectors.toList());

        ObjectNode result = Json.newObject();
        result.putPOJO("users", Json.toJson(professionalUsers));
        result.putPOJO("professionalUsers", Json.toJson(professionalUsersIds));
        return ok(result);
    }

    @ApiOperation(nickname = "updateProfessional", value = "Update professional",
            notes = "Allow admin to update an professional user.",
            httpMethod = "POST")
    @ApiImplicitParams(value= {
            @ApiImplicitParam(name = "body", defaultValue = "{\n" +
                    "  \"surname1\": \"Dummy\",\n" +
                    "  \"nationalId\": \"00000000C\"\n" +
                    "}", value="Object with post params", required = true, dataType = "Object", paramType = "body")})
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Params has not a required field."),
            @ApiResponse(code = 401, message = "Email or password is wrong."),
            @ApiResponse(code = 403, message = "Unauthorized.")})
    @AdminAuth
    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public static Result updateProfessional(
            @ApiParam(value = "Professional id", required = true) @PathParam("id") int uid) {
        User user = userService.getById(uid);

        if (user == null) return status(404, JsonUtils.simpleError("404", "Invalid identifier"));
        if (!(user instanceof Professional)) return status(403, JsonUtils.simpleError("403", "Unauthorized type user"));

        JsonNode json = request().body().asJson();
        json = JsonUtils.cleanFields((ObjectNode)json, ModelSecurity.ADMIN_MODIFIABLE_FIELDS);

        Iterator<String> fields = json.fieldNames();
        List<String> availableFields = new ArrayList<>();
        while(fields.hasNext()) availableFields.add(fields.next());

        // Parse the incoming data
        Professional professional;
        try {
            professional = Json.fromJson(json, Professional.class);
        } catch (RuntimeException e) {
            return status(400, JsonUtils.handleDeserializeException(e, "user"));
        }

        user.merge(professional, availableFields);

        ObjectNode result = Json.newObject();
        result.putPOJO("users", new ArrayNode(JsonNodeFactory.instance).add(Json.toJson(user)));

        return ok(result);
    }

    @ApiOperation(nickname = "banProfessional", value = "Ban professional",
            notes = "Allow the administrator to ban a professional user.",
            httpMethod = "POST")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "Email or password is wrong."),
            @ApiResponse(code = 403, message = "Unauthorized."),
            @ApiResponse(code = 403, message = "Unauthorized type user."),
            @ApiResponse(code = 404, message = "Invalid identifier.")})
    @AdminAuth
    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public static Result banProfessional(
            @ApiParam(value = "Professional id", required = true) @PathParam("id") int uid) {
        User user = userService.getById(uid);

        if (user == null) return status(404, JsonUtils.simpleError("404", "Invalid identifier"));
        if (!(user instanceof Professional)) return status(403, JsonUtils.simpleError("403", "Unauthorized type user"));

        user.setState(User.State.BANNED);

        ObjectNode result = Json.newObject();
        result.putPOJO("users", new ArrayNode(JsonNodeFactory.instance).add(Json.toJson(user)));

        return ok(result);
    }

    @ApiOperation(nickname = "deleteProfessional", value = "Delete Professional",
            notes = "Allow the administrator to delete a professional user.",
            httpMethod = "POST")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "Email or password is wrong."),
            @ApiResponse(code = 403, message = "Unauthorized."),
            @ApiResponse(code = 403, message = "Unauthorized type user."),
            @ApiResponse(code = 404, message = "Invalid identifier.")})
    @AdminAuth
    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public static Result deleteProfessional(
            @ApiParam(value = "Professional id", required = true) @PathParam("id") int uid) {
        User user = userService.getById(uid);

        if (user == null) return status(404, JsonUtils.simpleError("404", "Invalid identifier"));

        if (!(user instanceof Professional)) return status(403, JsonUtils.simpleError("403", "Unauthorized type user"));

        user.setState(User.State.DELETED);

        ObjectNode result = Json.newObject();
        result.putPOJO("users", new ArrayNode(JsonNodeFactory.instance).add(Json.toJson(user)));

        return ok(result);
    }
}

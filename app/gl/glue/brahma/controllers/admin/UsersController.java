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
        result.put("users", new ArrayNode(JsonNodeFactory.instance).add(Json.toJson(user)));
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

        ObjectNode result = Json.newObject();
        ArrayNode users = new ArrayNode(JsonNodeFactory.instance);
        users.add(Json.toJson(user));
        result.put("users", users);
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

        ObjectNode result = Json.newObject();
        ArrayNode users = new ArrayNode(JsonNodeFactory.instance);
        users.add(Json.toJson(professionalUsers));

        List<Integer> professionalUsersIds = professionalUsers.stream().map(o->o.getId()).collect(Collectors.toList());

        result.put("users", users);
        result.put("professionalUsers", Json.toJson(professionalUsersIds));
        return ok(result);
    }

    /**
     * @api {post} /admin/users/professional/update/:id Update
     *
     * @apiGroup Professional
     * @apiName Update
     * @apiDescription Allow admin user to update professional fields.
     *
     * @apiParam {String}               email       Optional. User email.
     * @apiParam {String}               login       Optional. User login.
     * @apiParam {Enum="MALE","FEMALE"} gender      Optional. User gender.
     * @apiParam {String}               name        Optional. Real user name.
     * @apiParam {Date}                 birthdate   Optional. Date of user birthdate.
     * @apiParam {String}               surname1    Optional. Real user first surname.
     * @apiParam {String}               surname2    Optional. Real user second surname.
     * @apiParam {String}               avatar      Optional. Url for user avatar.
     * @apiParam {String}               nationalId  Optional. Number iof id card.
     * @apiParam {String}               meta        Optional. Number iof id card.
     * @apiParamExample {json} Request-Example
     *      {
     *          "gender": "FEMALE",
     *          "name": "TestUpdated1",
     *          "birthdate": "1987-08-06",
     *          "surname1": "ProfessionalUpdated",
     *          "surname2": "User1Updated",
     *          "avatar": "http://...",
     *          "nationalId": "98765432Z",
     *          "meta": {}
     *      }
     *
     * @apiSuccess {Array} users    Contains all user fields after update.
     * @apiSuccessExample {json} Success-Response
     *      HTTP/1.1 200 OK
     *      {
     *          "users": [
     *              {
     *                  "id": 90005,
     *                  "login": null,
     *                  "email": "testprofessional1@glue.gl",
     *                  "name": "TestUpdated1",
     *                  "surname1": "ProfessionalUpdated",
     *                  "surname2": "User1Updated",
     *                  "birthdate": "1987-08-06",
     *                  "avatar": "http://...",
     *                  "nationalId": "98765432Z",
     *                  "gender": "FEMALE",
     *                  "state": "UNCONFIRMED",
     *                  "balance": 0,
     *                  "type": "professional"
     *                  "locked": false,
     *                  "confirmed": false,
     *                  "banned": false,
     *                  "meta": {}
     *              }
     *          ]
     *      }
     *
     * @apiError {Object} MissingRequiredField Params has not a required field.
     * @apiErrorExample {json} MissingRequiredField
     *      HTTP/1.1 400 BadRequest
     *      {
     *          "status": "400",
     *          "title": "Missing required field `field`"
     *      }
     *
     * @apiError {Object} UserNotLoggedIn User is not logged in.
     * @apiErrorExample {json} UserNotLoggedIn
     *      HTTP/1.1 401 Unauthorized
     *      {
     *          "status": "401",
     *          "title": "You are not logged in"
     *      }
     *
     * @apiError {Object} ForbiddenTypeUser Unauthorized type user
     * @apiErrorExample {json} ForbiddenTypeUser
     *      HTTP/1.1 403 Forbidden
     *      {
     *          "status": "403",
     *          "title": "Unauthorized type user"
     *      }
     *
     * @apiError UserNotFound The <code>state</code> contains a unknown value.
     * @apiErrorExample {json} UserNotFound
     *      HTTP/1.1 404 Not Found
     *      {
     *          "status": "404",
     *          "title": "Invalid identifier"
     *      }
     *
     * @apiVersion 0.1.0
     */
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
        result.put("users", new ArrayNode(JsonNodeFactory.instance).add(Json.toJson(user)));

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
        result.put("users", new ArrayNode(JsonNodeFactory.instance).add(Json.toJson(user)));

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
        result.put("users", new ArrayNode(JsonNodeFactory.instance).add(Json.toJson(user)));

        return ok(result);
    }
}

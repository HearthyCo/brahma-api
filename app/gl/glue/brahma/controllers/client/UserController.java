package gl.glue.brahma.controllers.client;

import actions.BasicAuth;
import actions.ClientAuth;
import actions.ProfessionalAuth;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gl.glue.brahma.model.user.Client;
import gl.glue.brahma.model.user.Professional;
import gl.glue.brahma.model.user.User;
import gl.glue.brahma.service.UserService;
import gl.glue.brahma.util.JsonUtils;
import gl.glue.brahma.util.ModelSecurity;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

import javax.persistence.PersistenceException;

public class UserController extends Controller {

    private static UserService userService = new UserService();

    /**
     * @api {post} /client/login Login
     *
     * @apiGroup Client
     * @apiName Login
     * @apiDescription Allow client (registered before) can be identified to access his private information.
     *
     * @apiParam {String} login     Unique identifier for user in service.
     * @apiParam {String} password  Password
     * @apiParamExample {json} Request-Example
     *      {
     *          "login": "client1",
     *          "password": "client1PasswordDummy"
     *      }
     *
     * @apiSuccess {object} user    Contains all user fields after login
     * @apiSuccessExample {json} Success-Response
     *      HTTP/1.1 200 OK
     *      {
     *          "users": [{
     *              "id": 1,
     *              "login": "client1",
     *              "name": "Client1",
     *              "surname1": "For",
     *              "surname2": "Service",
     *              "birthdate": "1987-08-06",
     *              "avatar": "http://...",
     *              "nationalId": "12345678A",
     *              "gender": "MALE",
     *              "meta": {}
     *          }]
     *      }
     *
     * @apiError {Object} MissingRequiredField Params has not a required field.
     * @apiError {Object} InvalidParams Username (login field) or password is wrong.
     * @apiErrorExample {json} MissingRequiredField
     *      HTTP/1.1 400 BadRequest
     *      {
     *          "status": "400",
     *          "title": "Missing required field `field`"
     *      }
     *
     * @apiErrorExample {json} InvalidParams
     *      HTTP/1.1 401 Unauthorized
     *      {
     *          "status": "401",
     *          "title": "Invalid username or password."
     *      }
     *
     * @apiVersion 0.1.0
     */
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

        if (!(user instanceof Client)) return status(403, JsonUtils.simpleError("403", "Unauthorized"));

        session().clear();
        session("id", Integer.toString(user.getId()));
        session("role", user.getClass().getSimpleName());

        result = Json.newObject();
        result.put("users", new ArrayNode(JsonNodeFactory.instance).add(Json.toJson(user)));

        return ok(result);
    }

    /**
     * @api {post} /client/me/register Register
     *
     * @apiGroup Client
     * @apiName Register
     * @apiDescription Allow client can to register in service.
     *
     * @apiParam {String}               email       Unique identifier for user in service.
     * @apiParam {String}               password    Password
     * @apiParam {Enum="MALE","FEMALE"} gender      Optional. User gender.
     * @apiParam {String}               name        Optional. Real user name.
     * @apiParam {Date}                 birthdate   Optional. Date of user birthdate.
     * @apiParam {String}               surname1    Optional. Real user first surname.
     * @apiParam {String}               surname2    Optional. Real user second surname.
     * @apiParam {String}               avatar      Optional. Url for user avatar.
     * @apiParam {String}               nationalId  Optional. Number iof id card.
     * @apiParam {Object}               meta        Optional. Other data still to be determined.
     * @apiParamExample {json} Request-Example
     *      {
     *          "email": "client1@example.com",
     *          "password": "client1PasswordDummy",
     *          "gender": "MALE",
     *          "name": "Client1",
     *          "birthdate": "1987-08-06",
     *          "surname1": "For",
     *          "surname2": "Service",
     *          "avatar": "http://...",
     *          "nationalId": "12345678A",
     *          "meta": {}
     *      }
     *
     * @apiSuccess {object} user    Contains all user fields after register.
     * @apiSuccessExample {json} Success-Response
     *      HTTP/1.1 200 OK
     *      {
     *          "users": [{
     *              "id": 1,
     *              "email": "client1@example.com",
     *              "name": "Client1",
     *              "surname1": "For",
     *              "surname2": "Service",
     *              "birthdate": "1987-08-06",
     *              "avatar": "http://...",
     *              "nationalId": "12345678A",
     *              "gender": "MALE",
     *              "meta": {}
     *          }]
     *      }
     *
     * @apiError {Object} MissingRequiredField Params has not a required field.
     * @apiError {Object} UserNameInUse Username (login field) is already in use.
     * @apiErrorExample {json} MissingRequiredField
     *      HTTP/1.1 400 BadRequest
     *      {
     *          "status": "400",
     *          "title": "Missing required field `field`"
     *      }
     *
     * @apiErrorExample {json} UserNameInUse
     *      HTTP/1.1 409 Conflict
     *      {
     *          "status": "409",
     *          "title": "Username already in use."
     *      }
     *
     * @apiVersion 0.1.0
     */
    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public static Result register() {

        JsonNode json = request().body().asJson();
        ObjectNode result = JsonUtils.checkRequiredFields(json, ModelSecurity.USER_REQUIRED_FIELDS);
        if (result != null) return badRequest(result);

        // Parse the incoming data
        Client client;
        try {
            json = JsonUtils.cleanFields((ObjectNode)json, ModelSecurity.USER_MODIFICABLE_FIELDS);
            client = Json.fromJson(json, Client.class);
        } catch (RuntimeException e) {
            return status(400, JsonUtils.handleDeserializeException(e, "user"));
        }

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
        session("role", user.getClass().getSimpleName());

        result = Json.newObject();
        result.put("users", new ArrayNode(JsonNodeFactory.instance).add(Json.toJson(user)));

        return ok(result);
    }

    /**
     * @api {post} /client/me/update Update
     *
     * @apiGroup Client
     * @apiName Update
     * @apiDescription Allow client user can to update fields profile.
     *
     * @apiParam {String}               email       Unique identifier for user in service.
     * @apiParam {String}               password    Password
     * @apiParam {Enum="MALE","FEMALE"} gender      Optional. User gender.
     * @apiParam {String}               name        Optional. Real user name.
     * @apiParam {Date}                 birthdate   Optional. Date of user birthdate.
     * @apiParam {String}               surname1    Optional. Real user first surname.
     * @apiParam {String}               surname2    Optional. Real user second surname.
     * @apiParam {String}               avatar      Optional. Url for user avatar.
     * @apiParam {String}               nationalId  Optional. Number iof id card.
     * @apiParamExample {json} Request-Example
     *      {
     *          "email": "client1@example.com",
     *          "password": "client1PasswordDummy",
     *          "gender": "MALE",
     *          "name": "Client1",
     *          "birthdate": "1987-08-06",
     *          "surname1": "For",
     *          "surname2": "Service",
     *          "avatar": "http://...",
     *          "nationalId": "12345678A",
     *          "meta": {}
     *      }
     *
     * @apiSuccess {object} user    Contains all user fields after register.
     * @apiSuccessExample {json} Success-Response
     *      HTTP/1.1 200 OK
     *      {
     *          "user": {
     *              "id": 1,
     *              "email": "client1@example.com",
     *              "name": "Client1",
     *              "surname1": "For",
     *              "surname2": "Service",
     *              "birthdate": "1987-08-06",
     *              "avatar": "http://...",
     *              "nationalId": "12345678A",
     *              "gender": "MALE",
     *              "meta": {}
     *          }
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
     * @apiVersion 0.1.0
     */
    @ClientAuth
    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public static Result update() {

        JsonNode json = request().body().asJson();
        json = JsonUtils.cleanFields((ObjectNode)json, ModelSecurity.USER_PROFILE_MODIFICABLE_FIELDS);

        // Parse the incoming data
        Client professional;
        try {
            professional = Json.fromJson(json, Client.class);
        } catch (RuntimeException e) {
            return status(400, JsonUtils.handleDeserializeException(e, "user"));
        }

        User user = userService.getById(Integer.parseInt(session("id")));
        user.merge(professional);

        ObjectNode result = Json.newObject();
        result.put("user", Json.toJson(user));

        return ok(result);
    }

    /**
     * @api {get} /user Get Me
     *
     * @apiGroup User
     * @apiName Get Me
     * @apiDescription Get the current logged in user.
     *
     * @apiSuccess {object} user    Contains all user fields
     * @apiSuccessExample {json} Success-Response
     *      HTTP/1.1 200 OK
     *      {
     *          "users": [{
     *              "id": 1,
     *              "email": "client1@example.com",
     *              "name": "Client1",
     *              "surname1": "For",
     *              "surname2": "Service",
     *              "birthdate": "1987-08-06",
     *              "avatar": "http://...",
     *              "nationalId": "12345678A",
     *              "gender": "MALE",
     *              "meta": {}
     *          }]
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
     */
    @ClientAuth
    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public static Result getMe() {

        User user = userService.getById(Integer.parseInt(session("id")));

        ObjectNode result = Json.newObject();
        result.put("users", new ArrayNode(JsonNodeFactory.instance).add(Json.toJson(user)));

        return ok(result);
    }

}
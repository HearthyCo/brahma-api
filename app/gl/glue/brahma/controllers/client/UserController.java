package gl.glue.brahma.controllers.client;

import actions.ClientAuth;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import play.mvc.Result;

import javax.persistence.PersistenceException;
import java.util.*;

public class UserController extends Controller {

    private static UserService userService = new UserService();

    /**
     * @api {post} /client/login Login
     *
     * @apiGroup Client
     * @apiName Login
     * @apiDescription Allow user (registered before) can be identified to access his private information.
     *
     * @apiParam {String} login     Unique identifier for user in service.
     * @apiParam {String} password  Password
     * @apiParamExample {json} Request-Example
     *      {
     *          "login": "testClient1@glue.gl",
     *          "password": "testClient1@glue.gl"
     *      }
     *
     * @apiSuccess {Array}  user    Contains all user fields after login
     * @apiSuccess {Array}  sign    Contains all signed user content index (sessions, id)
     * @apiSuccessExample {json} Success-Response
     *      HTTP/1.1 200 OK
     *      {
     *          "users": [
     *              {
     *                  "id": 90005,
     *                  "login": null,
     *                  "email": "testclient1@glue.gl",
     *                  "name": "Test",
     *                  "surname1": "Client",
     *                  "surname2": "User1",
     *                  "birthdate": "1969-12-31",
     *                  "avatar": "http://...",
     *                  "nationalId": "12345678A",
     *                  "gender": "MALE",
     *                  "state": "UNCONFIRMED",
     *                  "balance": 0,
     *                  "type": "client"
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
     * @apiError {Object} MissingRequiredField  Params has not a required field.
     * @apiErrorExample {json} MissingRequiredField
     *      HTTP/1.1 400 BadRequest
     *      {
     *          "status": "400",
     *          "title": "Missing required field `field`"
     *      }
     *
     * @apiError {Object} InvalidParams Username (login field) or password is wrong.
     * @apiErrorExample {json} InvalidParams
     *      HTTP/1.1 401 InvalidParams
     *      {
     *          "status": "401",
     *          "title": "Invalid username or password."
     *      }
     *
     * @apiError {Object} Forbidden  Unauthorized type user
     * @apiErrorExample {json} Forbidden
     *      HTTP/1.1 403 Forbidden
     *      {
     *          "status": "403",
     *          "title": "Unauthorized type user"
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
        if (!(user instanceof Client)) return status(403, JsonUtils.simpleError("403", "Unauthorized type user"));
        if (!user.canLogin()) return status(423, JsonUtils.simpleError("423", "Locked user"));

        session().clear();
        session("id", Integer.toString(user.getId()));
        session("role", user.getUserType().name());

        result = Json.newObject();
        result.put("users", new ArrayNode(JsonNodeFactory.instance).add(Json.toJson(user)));
        SignatureHelper.addSignatures(result, user.getId());
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
     *          "email": "testclient2@example.com",
     *          "password": "testclient2PasswordDummy",
     *      }
     *
     * @apiSuccess {Array}  user    Contains all user fields after login
     * @apiSuccess {Array}  sign    Contains all signed user content index (sessions, id)
     * @apiSuccessExample {json} Success-Response
     *      HTTP/1.1 200 OK
     *      {
     *          "users": [
     *              {
     *                  "id": 2,
     *                  "login": null,
     *                  "email": "testclient2@glue.gl",
     *                  "name": null,
     *                  "surname1": null,
     *                  "surname2": null,
     *                  "birthdate": null,
     *                  "avatar": null,
     *                  "nationalId": null,
     *                  "gender": "OTHER",
     *                  "gender": "OTHER",
     *                  "state": "UNCONFIRMED",
     *                  "balance": 0,
     *                  "type": "client"
     *                  "locked": false,
     *                  "confirmed": false,
     *                  "banned": false,
     *                  "meta": {},
     *              }
     *          ],
     *          "sign": [
     *              {
     *                  "id": "sessions",
     *                  "signature": "GtCU0kx60JDTQYiNVFzJkYQbGAJ/jsRVCz6lBNgXVNA=1426153660567",
     *                  "value": [ ]
     *              },
     *              {
     *                  "id": "userId",
     *                  "signature": "f1xdmk+xNP6mgWxK3v03MNUccyiUV+238NfwWsKdbeY=1426153660567",
     *                  "value": 2
     *              }
     *          ]
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
        session("role", user.getUserType().name());

        result = Json.newObject();
        result.put("users", new ArrayNode(JsonNodeFactory.instance).add(Json.toJson(user)));
        SignatureHelper.addSignatures(result, user.getId());
        return ok(result);
    }

    /**
     * @api {post} /client/me/update Update
     *
     * @apiGroup Client
     * @apiName Update
     * @apiDescription Allow client user can to update fields profile.
     *
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
     *          "surname1": "ClientUpdated",
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
     *                  "email": "testclient1@glue.gl",
     *                  "name": "TestUpdated1",
     *                  "surname1": "ClientUpdated",
     *                  "surname2": "User1Updated",
     *                  "birthdate": "1987-08-06",
     *                  "avatar": "http://...",
     *                  "nationalId": "98765432Z",
     *                  "gender": "OTHER",
     *                  "state": "UNCONFIRMED",
     *                  "balance": 0,
     *                  "type": "client"
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
     * @apiVersion 0.1.0
     */
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

        if (user.isLocked()) return status(423, JsonUtils.simpleError("423", "Locked or removed user"));

        user.merge(client, availableFields);

        ObjectNode result = Json.newObject();
        result.put("users", new ArrayNode(JsonNodeFactory.instance).add(Json.toJson(user)));

        return ok(result);
    }
}
package gl.glue.brahma.controllers.admin;

import actions.AdminAuth;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

public class UserController extends Controller {

    private static UserService userService = new UserService();

    /**
     * @api {post} /admin/login Login
     *
     * @apiGroup Admin
     * @apiName Login
     * @apiDescription Allow user (registered before) can be identified to access his private information.
     *
     * @apiParam {String} login     Unique identifier for user in service.
     * @apiParam {String} password  Password
     * @apiParamExample {json} Request-Example
     *      {
     *          "login": "testadmin1@glue.gl",
     *          "password": "testClient1@glue.gl"
     *      }
     *
     * @apiSuccess {Array}  user    Contains all user fields after login
     * @apiSuccessExample {json} Success-Response
     *      HTTP/1.1 200 OK
     *      {
     *          "users": [
     *              {
     *                  "id": 90010,
     *                  "login": null,
     *                  "email": "testadmin1@glue.gl",
     *                  "name": "Test",
     *                  "surname1": "Admin",
     *                  "surname2": "User1",
     *                  "birthdate": "1949-12-31",
     *                  "avatar": null,
     *                  "nationalId": "98765432J",
     *                  "gender": "OTHER",
     *                  "state": "UNCONFIRMED",
     *                  "balance": 0,
     *                  "type": "admin"
     *                  "locked": false,
     *                  "confirmed": false,
     *                  "banned": false,
     *                  "meta": {},
     *              }
     *          ],
     *          "sign": [
     *              {
     *                  "id": "userId",
     *                  "signature": "f1xdmk+xNP6mgWxK3v03MNUccyiUV+238NfwWsKdbeY=1426153660567",
     *                  "value": 2
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
     *      HTTP/1.1 401 Unauthorized
     *      {
     *          "status": "401",
     *          "title": "Invalid username or password."
     *      }
     *
     * @apiError {Object} ForbbidenTypeUser  Unauthorized type user
     * @apiErrorExample {json} ForbbidenTypeUser
     *      HTTP/1.1 403 Forbbiden
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
        if (!(user instanceof Admin)) return status(403, JsonUtils.simpleError("403", "Unauthorized type user"));
        if (!user.canLogin()) return status(423, JsonUtils.simpleError("423", "Locked user"));

        session().clear();
        session("id", Integer.toString(user.getId()));
        session("role", user.getType().name());

        result = Json.newObject();
        result.put("users", new ArrayNode(JsonNodeFactory.instance).add(Json.toJson(user)));
        SignatureHelper.addSignatures(result, user.getId());
        return ok(result);
    }

    /**
     * @api {post} /admin/me/update Update
     *
     * @apiGroup Admin
     * @apiName Update
     * @apiDescription Allow admin user can to update fields profile.
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
     *          "surname1": "AdminUpdated",
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
     *                  "email": "testadmin1@glue.gl",
     *                  "name": "TestUpdated1",
     *                  "surname1": "AdminUpdated",
     *                  "surname2": "User1Updated",
     *                  "birthdate": "1987-08-06",
     *                  "avatar": "http://...",
     *                  "nationalId": "98765432Z",
     *                  "gender": "OTHER",
     *                  "state": "UNCONFIRMED",
     *                  "balance": 0,
     *                  "type": "admin"
     *                  "locked": false,
     *                  "confirmed": false,
     *                  "banned": false,
     *                  "meta": {}
     *              }
     *          ],
     *          "sign": [
     *              {
     *                  "id": "userId",
     *                  "signature": "f1xdmk+xNP6mgWxK3v03MNUccyiUV+238NfwWsKdbeY=1426153660567",
     *                  "value": 2
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

        if (user.isLocked()) return status(423, JsonUtils.simpleError("423", "Locked or removed user"));

        user.merge(admin, availableFields);

        ObjectNode result = Json.newObject();
        result.put("users", new ArrayNode(JsonNodeFactory.instance).add(Json.toJson(user)));
        SignatureHelper.addSignatures(result, user.getId());
        return ok(result);
    }

    /**
     * @api {get} /admin/me Get Me
     *
     * @apiGroup Admin
     * @apiName Get Me
     * @apiDescription Get the current logged in admin.
     *
     * @apiSuccess {Array}  users   Contains all user fields after login
     * @apiSuccessExample {json} Success-Response
     *      HTTP/1.1 200 OK
     *      {
     *          "users": [
     *              {
     *                  "id": 90005,
     *                  "login": null,
     *                  "email": "testadmin1@glue.gl",
     *                  "name": "Test",
     *                  "surname1": "User",
     *                  "surname2": "User1",
     *                  "birthdate": "1969-12-31",
     *                  "avatar": "http://...",
     *                  "nationalId": "12345678A",
     *                  "gender": "OTHER",
     *                  "state": "UNCONFIRMED",
     *                  "balance": 0,
     *                  "type": "admin"
     *                  "locked": false,
     *                  "confirmed": false,
     *                  "banned": false,
     *                  "meta": {},
     *              }
     *          ],
     *          "sign": [
     *              {
     *                  "id": "userId",
     *                  "signature": "f1xdmk+xNP6mgWxK3v03MNUccyiUV+238NfwWsKdbeY=1426153660567",
     *                  "value": 2
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
     *      HTTP/1.1 423 Locked
     *      {
     *          "status": "423",
     *          "title": "Locked or removed user"
     *      }
     */
    @AdminAuth
    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public static Result getMe() {
        User user = userService.getById(Integer.parseInt(session("id")));

        if (user.isLocked()) return status(423, JsonUtils.simpleError("423", "Locked or removed user"));

        ObjectNode result = Json.newObject();
        ArrayNode users = new ArrayNode(JsonNodeFactory.instance);
        users.add(Json.toJson(user));
        result.put("users", users);
        SignatureHelper.addSignatures(result, user.getId());
        return ok(result);
    }
}

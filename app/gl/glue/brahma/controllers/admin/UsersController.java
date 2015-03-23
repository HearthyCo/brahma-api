package gl.glue.brahma.controllers.admin;


import actions.AdminAuth;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class UsersController extends Controller {

    private static UserService userService = new UserService();

    /**
     * @api {post} /admin/users/professional/create Create
     *
     * @apiGroup Admin
     * @apiName Create
     * @apiDescription Allow admin to create a professional user.
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
     *          "email": "newprofessiona1@glue.gl",
     *          "password": "newprofessiona1@glue.gl"
     *      }
     *
     * @apiSuccess {Array}  user    Contains all user fields
     * @apiSuccess {Array}  sign    Contains all signed user content index (sessions, id)
     * @apiSuccessExample {json} Success-Response
     *      HTTP/1.1 200 OK
     *      {
     *          "users": [
     *              {
     *                  "id": 3,
     *                  "login": null,
     *                  "email": "newprofessiona10@glue.gl",
     *                  "name": null,
     *                  "surname1": null,
     *                  "surname2": null,
     *                  "birthdate": null,
     *                  "avatar": null,
     *                  "nationalId": null,
     *                  "gender": "OTHER",
     *                  "state": "UNCONFIRMED",
     *                  "balance": 0,
     *                  "type": "professional"
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
     *                  "value": 3
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
     * @apiError {Object} UserNotLoggedIn User is not logged in.
     * @apiErrorExample {json} UserNotLoggedIn
     *      HTTP/1.1 401 Unauthorized
     *      {
     *          "status": "401",
     *          "title": "You are not logged in"
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

    /**
     * @api {get} /admin/users/professional/:id Get Professional
     *
     * @apiGroup Admin
     * @apiName Get Professional
     * @apiDescription Allow the administrator to get the professional user which has passed the parameter id
     *
     * @apiSuccess {Array}  users   Contains all user fields
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
     *                  "gender": "MALE",
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
     */
    @AdminAuth
    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public static Result readProfessional(int uid) {
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

    /**
     * @api {get} /admin/users/professional Get all Professionals
     *
     * @apiGroup Admin
     * @apiName Get all Professionals
     * @apiDescription Allow the administrator to get all professionals
     *
     * @apiSuccess {Array}  users               Contains all user fields
     * @apiSuccess {Array}  professionalUsers   Contains all professional ids
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
     *                  "gender": "MALE",
     *                  "state": "UNCONFIRMED",
     *                  "balance": 0,
     *                  "type": "user"
     *                  "locked": false,
     *                  "confirmed": false,
     *                  "banned": false,
     *                  "meta": {},
     *              }
     *          ],
     *          "professionalUsers": [
     *              90005
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
     * @apiError {Object} ForbiddenTypeUser Unauthorized type user
     * @apiErrorExample {json} ForbiddenTypeUser
     *      HTTP/1.1 403 Forbidden
     *      {
     *          "status": "403",
     *          "title": "Unauthorized type user"
     *      }
     *
     */
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
    @AdminAuth
    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public static Result updateProfessional(int uid) {
        User user = userService.getById(uid);

        if (user == null) return status(404, JsonUtils.simpleError("404", "Invalid identifier"));
        if (!(user instanceof Professional)) return status(403, JsonUtils.simpleError("403", "Unauthorized type user"));

        JsonNode json = request().body().asJson();
        json = JsonUtils.cleanFields((ObjectNode)json, ModelSecurity.ADMIN_MODIFIABLE_FIELDS);

        // Check if new email is already in use
        if (json.has("email")) {
            User userAux = userService.getByEmail(json.get("email").asText());
            if (userAux != null) return status(409, JsonUtils.simpleError("409", "Email already in use."));
        }

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

    /**
     * @api {post} /admin/users/professional/ban/:id Ban Professional
     *
     * @apiGroup Admin
     * @apiName Ban Professional
     * @apiDescription Allow the administrator to ban a professional user which has passed the parameter id
     *
     * @apiSuccess {Array}  users   Contains all user fields
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
     *                  "gender": "MALE",
     *                  "state": "BANNED",
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
     * @apiError {Object} UnauthorizedUser User is not logged in.
     * @apiErrorExample {json} UnauthorizedUser
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
     * @apiError {Object} LockedUser User is not logged in.
     * @apiErrorExample {json} LockedUser
     *      HTTP/1.1 403 Locked
     *      {
     *          "status": "403",
     *          "title": "Banned or removed user"
     *      }
     *
     */
    @AdminAuth
    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public static Result banProfessional(int uid) {
        User user = userService.getById(uid);

        if (user == null) return status(404, JsonUtils.simpleError("404", "Invalid identifier"));
        if (!(user instanceof Professional)) return status(403, JsonUtils.simpleError("403", "Unauthorized type user"));
        if (user.isLocked()) return status(403, JsonUtils.simpleError("403", "Banned or removed user"));

        user.setState(User.State.BANNED);

        ObjectNode result = Json.newObject();
        result.put("users", new ArrayNode(JsonNodeFactory.instance).add(Json.toJson(user)));

        return ok(result);
    }

    /**
     * @api {post} /admin/users/professional/delete/:id Delete Professional
     *
     * @apiGroup Admin
     * @apiName Delete Professional
     * @apiDescription Allow the administrator to delete a professional user which has passed the parameter id
     *
     * @apiSuccess {Array}  users   Contains all user fields
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
     *                  "gender": "MALE",
     *                  "state": "DELETED",
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
     * @apiError {Object} UnauthorizedUser User is not logged in.
     * @apiErrorExample {json} UnauthorizedUser
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
     */
    @AdminAuth
    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public static Result deleteProfessional(int uid) {
        User user = userService.getById(uid);

        if (user == null) return status(404, JsonUtils.simpleError("404", "Invalid identifier"));

        if (!(user instanceof Professional)) return status(403, JsonUtils.simpleError("403", "Unauthorized type user"));

        user.setState(User.State.DELETED);

        ObjectNode result = Json.newObject();
        result.put("users", new ArrayNode(JsonNodeFactory.instance).add(Json.toJson(user)));

        return ok(result);
    }
}

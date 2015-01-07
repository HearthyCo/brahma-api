package gl.glue.brahma.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gl.glue.brahma.model.user.Client;
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

    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public static Result login() {
        JsonNode json = request().body().asJson();
        ObjectNode result = JsonUtils.checkRequiredFields(json, "login", "password");
        if (result != null) return badRequest(result);
        String login = json.findPath("login").textValue();
        String pass = json.findPath("password").textValue();
        User user = userService.login(login, pass);
        if (user == null) {
            return status(401, JsonUtils.simpleError("401", "Invalid username or password."));
        }
        session().clear();
        session("login", user.getLogin());
        result = Json.newObject();
        result.put("users", Json.toJson(user));
        return ok(result);
    }

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
            return status(409, JsonUtils.simpleError("409", "Username already in use."));
        }
        // Also log him in
        session().clear();
        session("login", user.getLogin());
        result = Json.newObject();
        result.put("users", Json.toJson(user));
        return ok(result);
    }

}
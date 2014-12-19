package gl.glue.brahma.test;

import com.fasterxml.jackson.databind.node.ObjectNode;
import gl.glue.brahma.util.JsonUtils;
import org.junit.Test;
import play.libs.Json;
import play.mvc.Result;
import play.test.FakeRequest;
import scala.Option;

import static org.junit.Assert.*;
import static play.test.Helpers.*;

public class UserControllerTest extends TransactionalTest {

    private final int REQUEST_TIMEOUT = 1000;

    private ObjectNode toJson(Result result) {
        ObjectNode ret = JsonUtils.result2json(result);
        assertNotNull(ret);
        return ret;
    }

    private boolean hasCookies(Result result) {
        Option<String> cookie = result.toScala().header().headers().get("Set-Cookie");
        return !cookie.isEmpty() && !cookie.get().equals("");
    }

    private void assertError(ObjectNode ret, int status) {
        assertTrue(ret.has("errors"));
        assertEquals(Integer.toString(status), ret.get("errors").get(0).get("status").asText());
    }

    private Result makeLoginRequest(String login, String pass) {
        ObjectNode user = Json.newObject();
        user.put("login", login);
        user.put("password", pass);
        ObjectNode body = Json.newObject();
        body.put("user", user);
        FakeRequest fr = fakeRequest(POST, "/v1/user/login").withJsonBody(body);
        Result result = routeAndCall(fr, REQUEST_TIMEOUT);
        assertNotNull(result);
        return result;
    }

    @Test
    public void testLoginOk() {
        String login = "testClient1";
        Result result = makeLoginRequest(login, login);
        assertEquals(200, result.toScala().header().status());
        assertTrue(hasCookies(result));
        ObjectNode ret = toJson(result);
        assertTrue(ret.has("users"));
        assertEquals(login, ret.get("users").get("login").asText());
    }

    @Test
    public void testLoginIgnoresExtraFields() {
        String login = "testClient1";
        ObjectNode user = Json.newObject();
        user.put("login", login);
        user.put("password", login);
        user.put("canLogin", true);
        ObjectNode body = Json.newObject();
        body.put("user", user);
        FakeRequest fr = fakeRequest(POST, "/v1/user/login").withJsonBody(body);
        Result result = routeAndCall(fr, REQUEST_TIMEOUT);
        assertNotNull(result);
        assertEquals(200, result.toScala().header().status());
        assertTrue(hasCookies(result));
        ObjectNode ret = toJson(result);
        assertTrue(ret.has("users"));
        assertEquals(login, ret.get("users").get("login").asText());
    }

    @Test
    public void testLoginBadPass() {
        Result result = makeLoginRequest("testClient1", "bad-password");
        assertEquals(401, result.toScala().header().status());
        assertFalse(hasCookies(result));
        assertError(toJson(result), 401);
    }

    @Test
    public void testLoginBadUser() {
        Result result = makeLoginRequest("testNonexistentUser", "anyPassword");
        assertEquals(401, result.toScala().header().status());
        assertFalse(hasCookies(result));
        assertError(toJson(result), 401);
    }

    @Test
    public void testLoginBlockedUser() {
        Result result = makeLoginRequest("testPet1", "testPet1");
        assertEquals(401, result.toScala().header().status());
        assertFalse(hasCookies(result));
        assertError(toJson(result), 401);
    }

}

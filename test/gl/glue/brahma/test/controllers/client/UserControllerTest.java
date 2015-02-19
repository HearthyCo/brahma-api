package gl.glue.brahma.test.controllers.client;

import com.fasterxml.jackson.databind.node.ObjectNode;
import utils.TransactionalTest;
import org.junit.Test;
import play.libs.Json;
import play.mvc.Result;
import play.test.FakeRequest;
import utils.TestUtils;

import static org.junit.Assert.*;
import static play.test.Helpers.*;

public class UserControllerTest extends TransactionalTest {

    private final int REQUEST_TIMEOUT = 1000;

    @Test
    public void testLoginOk() {
        String login = "testClient1@glue.gl";
        Result result = TestUtils.makeClientLoginRequest(login, login);
        assertEquals(200, result.toScala().header().status());
        assertTrue(TestUtils.hasCookies(result));
        ObjectNode ret = TestUtils.toJson(result);

        assertTrue(ret.has("user"));
        assertEquals(login.toLowerCase(), ret.get("user").get("email").asText());
    }

    @Test
    public void testLoginIgnoresExtraFields() {
        String login = "testClient1@glue.gl";
        ObjectNode user = Json.newObject();
        user.put("email", login);
        user.put("password", login);
        user.put("canLogin", true);
        FakeRequest fr = fakeRequest(POST, "/v1/client/login").withJsonBody(user);
        Result result = routeAndCall(fr, REQUEST_TIMEOUT);
        assertNotNull(result);
        assertEquals(200, result.toScala().header().status());
        assertTrue(TestUtils.hasCookies(result));
        ObjectNode ret = TestUtils.toJson(result);
        assertTrue(ret.has("user"));
        assertEquals(login.toLowerCase(), ret.get("user").get("email").asText());
    }

    @Test
    public void testLoginBadPass() {
        Result result = TestUtils.makeClientLoginRequest("testClient1", "bad-password");
        assertEquals(401, result.toScala().header().status());
        assertFalse(TestUtils.hasCookies(result));
        TestUtils.assertError(401, TestUtils.toJson(result));
    }

    @Test
    public void testLoginBadUser() {
        Result result = TestUtils.makeClientLoginRequest("testNonexistentUser", "anyPassword");
        assertEquals(401, result.toScala().header().status());
        assertFalse(TestUtils.hasCookies(result));
        TestUtils.assertError(401, TestUtils.toJson(result));
    }

    @Test
    public void testLoginBlockedUser() {
        Result result = TestUtils.makeClientLoginRequest("testPet1", "testPet1");
        assertEquals(401, result.toScala().header().status());
        assertFalse(TestUtils.hasCookies(result));
        TestUtils.assertError(401, TestUtils.toJson(result));
    }

    //@Test // Disabled: there is some problem with transactions on this layer...
    public void testRegisterOk() {
        String login = "testNonexistentUser";
        ObjectNode user = Json.newObject();
        //user.put("login", login);
        user.put("email", login);
        user.put("password", "anyPassword");
        user.put("name", "testName");
        user.put("gender", "OTHER");
        user.put("birthdate", "1970-01-01");
        FakeRequest fr = fakeRequest(POST, "/v1/user").withJsonBody(user);
        Result result = routeAndCall(fr, REQUEST_TIMEOUT);
        assertNotNull(result);
        assertEquals(200, result.toScala().header().status());
        assertTrue(TestUtils.hasCookies(result));
        ObjectNode ret = TestUtils.toJson(result);
        assertTrue(ret.has("user"));
        assertEquals(login, ret.get("user").get("email").asText());
    }

}
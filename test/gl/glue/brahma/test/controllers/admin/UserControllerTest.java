package gl.glue.brahma.test.controllers.admin;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;
import play.libs.Json;
import play.mvc.Result;
import play.test.FakeRequest;
import utils.TestUtils;
import utils.TransactionalTest;

import static org.junit.Assert.*;
import static org.junit.Assert.assertNotNull;
import static play.test.Helpers.*;

public class UserControllerTest extends TransactionalTest {

    private final int REQUEST_TIMEOUT = 1000;

    @Test
    public void testLoginOk() {
        String login = "testAdmin1@glue.gl";
        String password = "testClient1@glue.gl";
        Result result = TestUtils.makeAdminLoginRequest(login, password);
        assertEquals(200, result.toScala().header().status());
        assertTrue(TestUtils.hasCookies(result));
        ObjectNode ret = TestUtils.toJson(result);

        assertTrue(ret.has("users"));
        assertEquals(login.toLowerCase(), ret.get("users").get(0).get("email").asText());
    }

    @Test
    public void testLoginIgnoresExtraFields() {
        String login = "testAdmin1@glue.gl";
        String password = "testClient1@glue.gl";
        ObjectNode user = Json.newObject();
        user.put("email", login);
        user.put("password", password);
        user.put("balance", 1000);
        FakeRequest fr = fakeRequest(POST, "/v1/admin/login").withJsonBody(user);
        Result result = routeAndCall(fr, REQUEST_TIMEOUT);
        assertNotNull(result);
        assertEquals(200, result.toScala().header().status());
        assertTrue(TestUtils.hasCookies(result));
        ObjectNode ret = TestUtils.toJson(result);
        assertTrue(ret.has("users"));
        assertEquals(login.toLowerCase(), ret.get("users").get(0).get("email").asText());
    }

    @Test
    public void testLoginBadPass() {
        Result result = TestUtils.makeAdminLoginRequest("testAdmin1@glue.gl", "testClientBadPass@glue.gl");
        assertEquals(401, result.toScala().header().status());
        assertFalse(TestUtils.hasCookies(result));
        TestUtils.assertError(401, TestUtils.toJson(result));
    }

    @Test
    public void testLoginBadUser() {
        Result result = TestUtils.makeAdminLoginRequest("testAdminBadUser@glue.gl", "testClient1@glue.gl");
        assertEquals(401, result.toScala().header().status());
        assertFalse(TestUtils.hasCookies(result));
        TestUtils.assertError(401, TestUtils.toJson(result));
    }

    @Test
    public void testLoginBlockedUser() {
        Result result = TestUtils.makeAdminLoginRequest("testhired1@glue.gl", "testhired1@glue.gl");
        assertEquals(401, result.toScala().header().status());
        assertFalse(TestUtils.hasCookies(result));
        TestUtils.assertError(401, TestUtils.toJson(result));
    }

    @Test
    public void testUpdateUserWithNotAllowedParams() {
        String login = "testAdmin1@glue.gl";
        String password = "testClient1@glue.gl";
        Result auth = TestUtils.makeAdminLoginRequest(login, password);

        String email = "dummyinvalidemail@dummy.dm";
        ObjectNode user = Json.newObject();
        user.put("email", email);

        Result result = TestUtils.callController(POST, "/v1/admin/me/update", auth, user);
        assertNotNull(result);
        assertEquals(200, result.toScala().header().status());

        ObjectNode ret = TestUtils.toJson(result);
        assertNotEquals(email, ret.get("users").get(0).get("email").asText());
        assertEquals(login.toLowerCase(), ret.get("users").get(0).get("email").asText());
    }

    @Test
    public void testUpdateUserOk() {
        String login = "testAdmin1@glue.gl";
        String password = "testClient1@glue.gl";
        Result auth = TestUtils.makeAdminLoginRequest(login, password);

        String surname1 = "Dummy";
        String surname2 = "Surname";
        ObjectNode user = Json.newObject();
        user.put("surname1", surname1);
        user.put("surname2", surname2);

        Result result = TestUtils.callController(POST, "/v1/admin/me/update", auth, user);
        assertNotNull(result);
        assertEquals(200, result.toScala().header().status());

        ObjectNode ret = TestUtils.toJson(result);
        assertEquals(surname1, ret.get("users").get(0).get("surname1").asText());
        assertEquals(surname2, ret.get("users").get(0).get("surname2").asText());
    }

    @Test
    public void testGetMeUnauthenticated() {
        FakeRequest fr = fakeRequest(GET, "/v1/admin/me");
        Result result = routeAndCall(fr, REQUEST_TIMEOUT);
        assertNotNull(result);
        assertEquals(401, result.toScala().header().status());
    }

    @Test
    public void testGetMeUserBlock() {
        String login = "testadminbanned@glue.gl";
        String password = "testClient1@glue.gl";
        Result auth = TestUtils.makeAdminLoginRequest(login, password);

        Result result = TestUtils.callController(GET, "/v1/admin/me", auth);

        assertNotNull(result);
        assertEquals(401, result.toScala().header().status());
    }

    @Test
    public void testGetMeUserOk() {
        String login = "testadmin1@glue.gl";
        String password = "testClient1@glue.gl";
        Result auth = TestUtils.makeAdminLoginRequest(login, password);

        Result result = TestUtils.callController(GET, "/v1/admin/me", auth);
        assertNotNull(result);
        assertEquals(200, result.toScala().header().status());

        ObjectNode ret = TestUtils.toJson(result);
        assertEquals(login, ret.get("users").get(0).get("email").asText());
    }
}

package gl.glue.brahma.test.controllers.professional;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;
import play.libs.Json;
import play.mvc.Result;
import play.test.FakeRequest;
import utils.TestUtils;
import utils.TransactionalTest;

import static org.junit.Assert.*;
import static play.test.Helpers.*;

public class UserControllerTest extends TransactionalTest {

    private final int REQUEST_TIMEOUT = 1000;

    @Test
    public void testLoginOk() {
        String login = "testProfessional1@glue.gl";
        Result result = TestUtils.makeProfessionalLoginRequest(login, login);
        assertEquals(200, result.toScala().header().status());
        assertTrue(TestUtils.hasCookies(result));
        ObjectNode ret = TestUtils.toJson(result);

        assertTrue(ret.has("users"));
        assertEquals(login.toLowerCase(), ret.get("users").get(0).get("email").asText());
    }

    @Test
    public void testLoginIgnoresExtraFields() {
        String login = "testProfessional1@glue.gl";
        ObjectNode user = Json.newObject();
        user.put("email", login);
        user.put("password", login);
        user.put("balance", 1000);
        FakeRequest fr = fakeRequest(POST, "/v1/professional/login").withJsonBody(user);
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
        Result result = TestUtils.makeProfessionalLoginRequest("testProfessional1", "bad-password");
        assertEquals(401, result.toScala().header().status());
        assertFalse(TestUtils.hasCookies(result));
        TestUtils.assertError(401, TestUtils.toJson(result));
    }

    @Test
    public void testLoginBadUser() {
        Result result = TestUtils.makeProfessionalLoginRequest("testNonexistentUser", "anyPassword");
        assertEquals(401, result.toScala().header().status());
        assertFalse(TestUtils.hasCookies(result));
        TestUtils.assertError(401, TestUtils.toJson(result));
    }

    @Test
    public void testLoginBlockedUser() {
        Result result = TestUtils.makeProfessionalLoginRequest("testPet1", "testPet1");
        assertEquals(401, result.toScala().header().status());
        assertFalse(TestUtils.hasCookies(result));
        TestUtils.assertError(401, TestUtils.toJson(result));
    }

    @Test
    public void testUpdateUserWithNotAllowedParams() {
        String login = "testProfessional1@glue.gl";
        Result auth = TestUtils.makeProfessionalLoginRequest(login, login);

        String email = "dummyinvalidemail@dummy.dm";
        ObjectNode user = Json.newObject();
        user.put("email", email);

        Result result = TestUtils.callController(POST, "/v1/professional/me/update", auth, user);
        assertNotNull(result);
        assertEquals(200, result.toScala().header().status());

        ObjectNode ret = TestUtils.toJson(result);
        assertNotEquals(email, ret.get("users").get(0).get("email").asText());
        assertEquals(login.toLowerCase(), ret.get("users").get(0).get("email").asText());
    }

    @Test
    public void testUpdateUserOk() {
        String login = "testProfessional1@glue.gl";
        Result auth = TestUtils.makeProfessionalLoginRequest(login, login);

        String surname1 = "Dummy";
        String surname2 = "Surname";
        ObjectNode user = Json.newObject();
        user.put("surname1", surname1);
        user.put("surname2", surname2);

        Result result = TestUtils.callController(POST, "/v1/professional/me/update", auth, user);
        assertNotNull(result);
        assertEquals(200, result.toScala().header().status());

        ObjectNode ret = TestUtils.toJson(result);
        assertEquals(surname1, ret.get("users").get(0).get("surname1").asText());
        assertEquals(surname2, ret.get("users").get(0).get("surname2").asText());
    }

    @Test
    public void testGetMeUnauthenticated() {
        FakeRequest fr = fakeRequest(GET, "/v1/professional/me");
        Result result = routeAndCall(fr, REQUEST_TIMEOUT);
        assertNotNull(result);
        assertEquals(401, result.toScala().header().status());
    }

    @Test
    public void testGetMeUserBlock() {
        String login = "testhired1@glue.gl";
        String password = "testHired1@glue.gl";
        Result auth = TestUtils.makeProfessionalLoginRequest(login, password);

        Result result = TestUtils.callController(GET, "/v1/professional/me", auth);

        assertNotNull(result);
        assertEquals(401, result.toScala().header().status());
    }

    @Test
    public void testGetMeUserOk() {
        String login = "testprofessional1@glue.gl";
        String password = "testProfessional1@glue.gl";
        Result auth = TestUtils.makeProfessionalLoginRequest(login, password);

        Result result = TestUtils.callController(GET, "/v1/professional/me", auth);
        assertNotNull(result);
        assertEquals(200, result.toScala().header().status());

        ObjectNode ret = TestUtils.toJson(result);
        assertEquals(login, ret.get("users").get(0).get("email").asText());
    }
}

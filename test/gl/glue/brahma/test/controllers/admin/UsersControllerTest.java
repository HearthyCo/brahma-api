package gl.glue.brahma.test.controllers.admin;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;
import play.Logger;
import play.libs.Json;
import play.mvc.Result;
import play.test.FakeRequest;
import utils.TestUtils;
import utils.TransactionalTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static play.mvc.Http.Status.REQUEST_TIMEOUT;
import static play.test.Helpers.*;

public class UsersControllerTest extends TransactionalTest {

    String loginValid = "testadmin1@glue.gl";
    String passwordValid = "testClient1@glue.gl";

    @Test
    public void testCreatedProfessionalUnauthenticated() {
        FakeRequest fr = fakeRequest(POST, "/v1/admin/users/professional/create");
        Result result = routeAndCall(fr, REQUEST_TIMEOUT);
        assertNotNull(result);
        assertEquals(401, result.toScala().header().status());
    }

    @Test
    public void testCreatedProfessionalWithNonAdminUser() {
        String login = "testprofessional1@glue.gl";
        String password = "testProfessional1@glue.gl";

        Result auth = TestUtils.makeProfessionalLoginRequest(login, password);

        String email = "dummyemail@dummy.dm";
        ObjectNode user = Json.newObject();
        user.put("email", email);
        user.put("password", email);

        Result result = TestUtils.callController(POST, "/v1/admin/users/professional/create", auth, user);
        assertNotNull(result);
        assertEquals(403, result.toScala().header().status());
    }

    @Test
    public void testCreatedProfessionalWithoutRequiredParams() {
        Result auth = TestUtils.makeAdminLoginRequest(loginValid, passwordValid);

        ObjectNode user = Json.newObject();
        user.put("password", passwordValid);

        Result result = TestUtils.callController(POST, "/v1/admin/users/professional/create", auth, user);
        assertNotNull(result);
        assertEquals(400, result.toScala().header().status());
    }

    @Test
    public void testCreatedProfessionalWithUsedEmail() {
        Result auth = TestUtils.makeAdminLoginRequest(loginValid, passwordValid);

        String email = "testProfessional1@glue.gl";
        ObjectNode user = Json.newObject();
        user.put("email", email);
        user.put("password", email);

        Result result = TestUtils.callController(POST, "/v1/admin/users/professional/create", auth, user);
        assertNotNull(result);
        assertEquals(409, result.toScala().header().status());
    }

    @Test
    public void testCreatedProfessionalOk() {
        Result auth = TestUtils.makeAdminLoginRequest(loginValid, passwordValid);

        String email = "testNewAdmin10@glue.gl";
        ObjectNode user = Json.newObject();
        user.put("email", email);
        user.put("password", email);

        Result result = TestUtils.callController(POST, "/v1/admin/users/professional/create", auth, user);
        assertNotNull(result);
        assertEquals(200, result.toScala().header().status());

        ObjectNode ret = TestUtils.toJson(result);
        assertNotEquals(email, ret.get("users").get(0).get("email").asText());
        assertEquals("UNCONFIRMED", ret.get("users").get(0).get("state").asText());
    }

    @Test
    public void testGetProfessionalUnauthenticated() {
        FakeRequest fr = fakeRequest(GET, "/v1/admin/users/professional/90006");
        Result result = routeAndCall(fr, REQUEST_TIMEOUT);
        assertNotNull(result);
        assertEquals(401, result.toScala().header().status());
    }

    @Test
    public void testGetProfessionalWithNonAdminUser() {
        String login = "testprofessional1@glue.gl";
        String password = "testProfessional1@glue.gl";
        Result auth = TestUtils.makeProfessionalLoginRequest(login, password);

        Logger.info("LOGIN " + auth.toScala().header().status());

        Result result = TestUtils.callController(GET, "/v1/admin/users/professional/90006", auth);
        assertNotNull(result);
        assertEquals(403, result.toScala().header().status());
    }

    @Test
    public void testGetProfessionalInvalidId() {
        Result auth = TestUtils.makeAdminLoginRequest(loginValid, passwordValid);

        Result result = TestUtils.callController(GET, "/v1/admin/users/professional/99999", auth);
        assertNotNull(result);
        assertEquals(404, result.toScala().header().status());
    }

    @Test
    public void testGetProfessionalNotAllowedTypeUser() {
        Result auth = TestUtils.makeAdminLoginRequest(loginValid, passwordValid);

        Result result = TestUtils.callController(GET, "/v1/admin/users/professional/90000", auth);
        assertNotNull(result);
        assertEquals(403, result.toScala().header().status());
    }

    @Test
    public void testGetProfessionalOk() {
        Result auth = TestUtils.makeAdminLoginRequest(loginValid, passwordValid);

        Result result = TestUtils.callController(GET, "/v1/admin/users/professional/90006", auth);
        assertNotNull(result);
        assertEquals(200, result.toScala().header().status());

        ObjectNode ret = TestUtils.toJson(result);
        assertNotEquals(loginValid, ret.get("users").get(0).get("email").asText());
    }

    @Test
    public void testGetProfessionalsOk() {
        Result auth = TestUtils.makeAdminLoginRequest(loginValid, passwordValid);

        Result result = TestUtils.callController(GET, "/v1/admin/users/professional", auth);
        assertNotNull(result);
        assertEquals(200, result.toScala().header().status());

        ObjectNode ret = TestUtils.toJson(result);
        assertEquals(4, ret.get("users").size());
    }

    @Test
    public void testUpdateProfessionalUnauthenticated() {
        FakeRequest fr = fakeRequest(POST, "/v1/admin/users/professional/update/90006");
        Result result = routeAndCall(fr, REQUEST_TIMEOUT);
        assertNotNull(result);
        assertEquals(401, result.toScala().header().status());
    }

    @Test
    public void testUpdateProfessionalWithNonAdminUser() {
        String login = "testprofessional1@glue.gl";
        String password = "testProfessional1@glue.gl";
        Result auth = TestUtils.makeProfessionalLoginRequest(login, password);

        ObjectNode user = Json.newObject();
        user.put("name", "DummyName");
        user.put("surname1", "DummeSurname1");

        Result result = TestUtils.callController(POST, "/v1/admin/users/professional/update/90006", auth, user);
        assertNotNull(result);
        assertEquals(403, result.toScala().header().status());
    }

    @Test
    public void testUpdateProfessionalInvalidId() {
        Result auth = TestUtils.makeAdminLoginRequest(loginValid, passwordValid);

        ObjectNode user = Json.newObject();
        user.put("name", "DummyName");
        user.put("surname1", "DummeSurname1");

        Result result = TestUtils.callController(POST, "/v1/admin/users/professional/update/99999", auth, user);
        assertNotNull(result);
        assertEquals(404, result.toScala().header().status());
    }

    @Test
    public void testUpdateProfessionalNotAllowedTypeUser() {
        Result auth = TestUtils.makeAdminLoginRequest(loginValid, passwordValid);

        String name = "Updated name";
        ObjectNode user = Json.newObject();
        user.put("name", name);
        user.put("surname1", name);

        Result result = TestUtils.callController(POST, "/v1/admin/users/professional/update/90000", auth, user);
        assertNotNull(result);
        assertEquals(403, result.toScala().header().status());
    }

    @Test
    public void testUpdateLockedProfessional() {
        // Banned professionals can be updated by an admin.
        Result auth = TestUtils.makeAdminLoginRequest(loginValid, passwordValid);

        String name = "Updated name";
        ObjectNode user = Json.newObject();
        user.put("name", name);
        user.put("surname1", name);

        Result result = TestUtils.callController(POST, "/v1/admin/users/professional/update/90009", auth, user);
        assertNotNull(result);
        assertEquals(200, result.toScala().header().status());

        ObjectNode ret = TestUtils.toJson(result);
        assertEquals(name, ret.get("users").get(0).get("name").asText());
        assertEquals(name, ret.get("users").get(0).get("surname1").asText());
    }

    @Test
    public void testUpdateProfessionalOk() {
        Result auth = TestUtils.makeAdminLoginRequest(loginValid, passwordValid);

        String name = "Updated name";
        ObjectNode user = Json.newObject();
        user.put("name", name);
        user.put("surname1", name);

        Result result = TestUtils.callController(POST, "/v1/admin/users/professional/update/90006", auth, user);
        assertNotNull(result);
        assertEquals(200, result.toScala().header().status());

        ObjectNode ret = TestUtils.toJson(result);
        assertEquals(name, ret.get("users").get(0).get("name").asText());
        assertEquals(name, ret.get("users").get(0).get("surname1").asText());
    }

    @Test
    public void testDeleteProfessionalUnauthenticated() {
        FakeRequest fr = fakeRequest(POST, "/v1/admin/users/professional/delete/90006");
        Result result = routeAndCall(fr, REQUEST_TIMEOUT);
        assertNotNull(result);
        assertEquals(401, result.toScala().header().status());
    }

    @Test
    public void testDeleteProfessionalWithNonAdminUser() {
        String login = "testprofessional1@glue.gl";
        String password = "testProfessional1@glue.gl";
        Result auth = TestUtils.makeProfessionalLoginRequest(login, password);

        Result result = TestUtils.callController(POST, "/v1/admin/users/professional/delete/90006", auth);
        assertNotNull(result);
        assertEquals(403, result.toScala().header().status());
    }

    @Test
    public void testDeleteProfessionalInvalidId() {
        Result auth = TestUtils.makeAdminLoginRequest(loginValid, passwordValid);

        Result result = TestUtils.callController(POST, "/v1/admin/users/professional/delete/99999", auth);
        assertNotNull(result);
        assertEquals(404, result.toScala().header().status());
    }

    @Test
    public void testDeleteProfessionalNotAllowedTypeUser() {
        Result auth = TestUtils.makeAdminLoginRequest(loginValid, passwordValid);

        Result result = TestUtils.callController(POST, "/v1/admin/users/professional/delete/90000", auth);
        assertNotNull(result);
        assertEquals(403, result.toScala().header().status());
    }

    @Test
    public void testDeleteProfessionalOk() {
        Result auth = TestUtils.makeAdminLoginRequest(loginValid, passwordValid);

        Result result = TestUtils.callController(POST, "/v1/admin/users/professional/delete/90006", auth);
        assertNotNull(result);
        assertEquals(200, result.toScala().header().status());

        ObjectNode ret = TestUtils.toJson(result);
        assertNotEquals(loginValid, ret.get("users").get(0).get("email").asText());
        assertEquals("DELETED", ret.get("users").get(0).get("state").asText());
    }

    @Test
    public void testBanProfessionalUnauthenticated() {
        FakeRequest fr = fakeRequest(POST, "/v1/admin/users/professional/ban/90006");
        Result result = routeAndCall(fr, REQUEST_TIMEOUT);
        assertNotNull(result);
        assertEquals(401, result.toScala().header().status());
    }

    @Test
    public void testBanProfessionalWithNonAdminUser() {
        String login = "testprofessional1@glue.gl";
        String password = "testProfessional1@glue.gl";
        Result auth = TestUtils.makeProfessionalLoginRequest(login, password);

        Logger.info("USER " + auth.toScala().header().status());

        Result result = TestUtils.callController(POST, "/v1/admin/users/professional/ban/90006", auth);
        assertNotNull(result);
        assertEquals(403, result.toScala().header().status());
    }


    @Test
    public void testBanProfessionalInvalidId() {
        Result auth = TestUtils.makeAdminLoginRequest(loginValid, passwordValid);

        Result result = TestUtils.callController(POST, "/v1/admin/users/professional/ban/99999", auth);
        assertNotNull(result);
        assertEquals(404, result.toScala().header().status());
    }

    @Test
    public void testBanProfessionalNotAllowedTypeUser() {
        Result auth = TestUtils.makeAdminLoginRequest(loginValid, passwordValid);

        Result result = TestUtils.callController(POST, "/v1/admin/users/professional/ban/90000", auth);
        assertNotNull(result);
        assertEquals(403, result.toScala().header().status());
    }

    @Test
    public void testBanProfessionalOk() {
        Result auth = TestUtils.makeAdminLoginRequest(loginValid, passwordValid);

        Result result = TestUtils.callController(POST, "/v1/admin/users/professional/ban/90006", auth);
        assertNotNull(result);
        assertEquals(200, result.toScala().header().status());

        ObjectNode ret = TestUtils.toJson(result);
        assertNotEquals(loginValid, ret.get("users").get(0).get("email").asText());
        assertEquals("BANNED", ret.get("users").get(0).get("state").asText());

    }
}

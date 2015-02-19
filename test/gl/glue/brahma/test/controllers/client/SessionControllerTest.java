package gl.glue.brahma.test.controllers.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.FluentIterable;
import utils.TransactionalTest;
import org.junit.Test;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.FakeRequest;
import utils.TestUtils;

import static org.junit.Assert.*;
import static play.test.Helpers.*;

public class SessionControllerTest extends TransactionalTest {

    @Test // Request new session without params, must return null
    public void testNewSessionWithoutParams() {
        String login = "testClient1@glue.gl";
        Result auth = TestUtils.makeClientLoginRequest(login, login);
        Result result = TestUtils.callController(POST, "/v1/client/session/create", auth, Json.newObject());

        assertEquals(400, result.toScala().header().status());
    }

    @Test // Request new session without autentication, must retrun 401
    public void testNewSessionUnauthenticated() {
        FakeRequest fr = fakeRequest(POST, "/v1/client/session/create");
        Result result = routeAndCall(fr, REQUEST_TIMEOUT);
        assertNotNull(result);
        assertEquals(401, result.toScala().header().status());
    }

    @Test // Request new session with an invalid date (programmed is before that now date), must return 400
    public void testNewBookSessionInvalidDate() {
        String login = "testClient1@glue.gl";
        Result auth = TestUtils.makeClientLoginRequest(login, login);

        ObjectNode user = Json.newObject();
        user.put("service", 90302);
        user.put("startDate", 1423653792229L);

        Result result = TestUtils.callController(POST, "/v1/client/session/book", auth, user);
        assertNotNull(result);
        assertEquals(400, result.toScala().header().status());
    }

    //@Test // Request new valid session // Disabled because of rollback bug at controllers
    public void testNewProgrammedSession() {
        String login = "testClient1@glue.gl";
        Result auth = TestUtils.makeClientLoginRequest(login, login);

        int service = 90302;
        String state = "REQUESTED";
        ObjectNode user = Json.newObject();
        user.put("service", service);
        user.put("state", state);

        Result result = TestUtils.callController(POST, "/v1/client/session", auth, user);
        assertNotNull(result);

        ObjectNode ret = TestUtils.toJson(result);

        ObjectNode session = (ObjectNode) ret.get("session");

        assertEquals(state, session.get("state").asText());
        assertEquals(true, session.get("isNew").asBoolean());
    }

    @Test // Request without id param
    public void testGetSessionWithoutParams() {
        String login = "testClient1@glue.gl";
        Result auth = TestUtils.makeClientLoginRequest(login, login);

        Http.Cookie[] cookies = FluentIterable.from(cookies(auth)).toArray(Http.Cookie.class);

        FakeRequest fr = fakeRequest(GET, "/v1/client/session/").withCookies(cookies);
        Result result = routeAndCall(fr, REQUEST_TIMEOUT);
        assertNull(result);
    }

    @Test // Request without user authentication
    public void testGetSessionUnauthenticated() {
        int id = 90700;
        FakeRequest fr = fakeRequest(GET, "/v1/client/session/" + id);
        Result result = routeAndCall(fr, REQUEST_TIMEOUT);
        assertNotNull(result);
        assertEquals(401, result.toScala().header().status());
    }

    @Test // Request with invalid user Authentication. User "testClient2" is not an user for session 90700
    public void testGetSessionUnauthorized() {
        String login = "testClient2@glue.gl";
        Result auth = TestUtils.makeClientLoginRequest(login, login);

        Result result = TestUtils.callController(GET, "/v1/client/session/90700", auth);
        assertNotNull(result);
        assertEquals(404, result.toScala().header().status());
    }

    @Test // Request with an non-existent session id
    public void testGetSessionInvalidId() {
        String login = "testClient1@glue.gl";
        Result auth = TestUtils.makeClientLoginRequest(login, login);

        Result result = TestUtils.callController(GET, "/v1/client/session/0", auth);
        assertNotNull(result);
        assertEquals(404, result.toScala().header().status());
    }

    @Test // Valid request
    public void testGetSessionOk() {
        String login = "testClient1@glue.gl";
        Result auth = TestUtils.makeClientLoginRequest(login, login);

        int id = 90700;
        Result result = TestUtils.callController(GET, "/v1/client/session/" + id, auth);
        ObjectNode ret = TestUtils.toJson(result);

        assertNotNull(result);
        assertEquals(200, result.toScala().header().status());
        assertEquals(id, ret.get("session").get("id").asInt());
    }

    @Test // Request with an invalid session state
    public void testGetSessionsByStateInvalidState() {
        String login = "testClient1@glue.gl";
        Result auth = TestUtils.makeClientLoginRequest(login, login);

        String state = "dummystate";
        Result result = TestUtils.callController(GET, "/v1/client/me/sessions/" + state, auth);

        assertNull(result);
    }

    @Test // Request session with an valid programmed state
    public void testGetSessionsByStateProgrammed() {
        String login = "testClient1@glue.gl";
        Result auth = TestUtils.makeClientLoginRequest(login, login);

        String state = "programmed";
        Result result = TestUtils.callController(GET, "/v1/client/me/sessions/" + state, auth);
        ObjectNode ret = TestUtils.toJson(result);

        assertNotNull(result);
        assertEquals(200, result.toScala().header().status());
        assertEquals(2, ret.get("sessions").size());

        for(JsonNode session : ret.get("sessions")) {
            String stateSession = session.get("state").asText();
            assertTrue(stateSession.equals("PROGRAMMED"));
        }
    }

    @Test // Request session with an valid underway state (Count 0)
    public void testGetSessionsByStateUnderway() {
        String login = "testClient1@glue.gl";
        Result auth = TestUtils.makeClientLoginRequest(login, login);

        String state = "underway";
        Result result = TestUtils.callController(GET, "/v1/client/me/sessions/" + state, auth);
        ObjectNode ret = TestUtils.toJson(result);

        assertNotNull(result);
        assertEquals(200, result.toScala().header().status());
        assertEquals(1, ret.get("sessions").size());
    }

    @Test // Request session with closed (closed and finished) state
    public void testGetSessionsByStateClosed() {
        String login = "testClient1@glue.gl";
        Result auth = TestUtils.makeClientLoginRequest(login, login);

        String state = "closed";
        Result result = TestUtils.callController(GET, "/v1/client/me/sessions/" + state, auth);
        ObjectNode ret = TestUtils.toJson(result);

        assertNotNull(result);
        assertEquals(200, result.toScala().header().status());
        assertEquals(2, ret.get("sessions").size());

        for(JsonNode session : ret.get("sessions")) {
            String stateSession = session.get("state").asText();
            assertTrue(stateSession.equals("CLOSED") || stateSession.equals("FINISHED"));
        }
    }

}
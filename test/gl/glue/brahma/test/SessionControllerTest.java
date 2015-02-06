package gl.glue.brahma.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.FluentIterable;
import org.junit.Test;
import play.mvc.Http;
import play.mvc.Result;
import play.test.FakeRequest;
import utils.TestUtils;

import static org.junit.Assert.*;
import static play.test.Helpers.*;

public class SessionControllerTest extends TransactionalTest {




    @Test // Request without id param
    public void requestSessionWithoutParams() {
        String login = "testClient1@glue.gl";
        Result responseLogin = TestUtils.makeLoginRequest(login, login);

        Http.Cookie[] cookies = FluentIterable.from(cookies(responseLogin)).toArray(Http.Cookie.class);

        FakeRequest fr = fakeRequest(GET, "/v1/session/").withCookies(cookies);
        Result result = routeAndCall(fr, REQUEST_TIMEOUT);
        assertNull(result);
    }

    @Test // Request without user authentication
    public void requestSessionWithoutAuthentication() {
        int id = 90700;
        FakeRequest fr = fakeRequest(GET, "/v1/session/" + id);
        Result result = routeAndCall(fr, REQUEST_TIMEOUT);
        assertNotNull(result);
        assertEquals(401, result.toScala().header().status());
    }

    @Test // Request with invalid user Authentication. User "testClient2" is not an user for session 90700
    public void requestSessionWithInvalidAuthentication() {
        String login = "testClient2@glue.gl";
        Result responseLogin = TestUtils.makeLoginRequest(login, login);

        Result result = TestUtils.getSessionRequest(90700, responseLogin);
        assertNotNull(result);
        assertEquals(404, result.toScala().header().status());
    }

    @Test // Request with an non-existent session id
    public void requestSessionInvalidId() {
        String login = "testClient1@glue.gl";
        Result responseLogin = TestUtils.makeLoginRequest(login, login);

        Result result = TestUtils.getSessionRequest(0, responseLogin);
        assertNotNull(result);
        assertEquals(404, result.toScala().header().status());
    }

    @Test // Valid request
    public void requestSessionOk() {
        String login = "testClient1@glue.gl";
        Result responseLogin = TestUtils.makeLoginRequest(login, login);

        int id = 90700;
        Result result = TestUtils.getSessionRequest(id, responseLogin);
        ObjectNode ret = TestUtils.toJson(result);

        assertNotNull(result);
        assertEquals(200, result.toScala().header().status());
        assertEquals(id, ret.get("session").get("id").asInt());
    }

    @Test // Request with an invalid session state
    public void requestInvalidSessionState() {
        String login = "testClient1@glue.gl";
        Result responseLogin = TestUtils.makeLoginRequest(login, login);

        String state = "dummystate";
        Result result = TestUtils.getSessionStateRequest(state, responseLogin);

        assertNull(result);
    }

    @Test // Request session with an valid programmed state
    public void requestProgrammedSession() {
        String login = "testClient1@glue.gl";
        Result responseLogin = TestUtils.makeLoginRequest(login, login);

        String state = "programmed";
        Result result = TestUtils.getSessionStateRequest(state, responseLogin);
        ObjectNode ret = TestUtils.toJson(result);

        assertNotNull(result);
        assertEquals(200, result.toScala().header().status());
        assertEquals(1, ret.get("sessions").size());

        for(JsonNode session : ret.get("sessions")) {
            String stateSession = session.get("state").asText();
            assertTrue(stateSession.equals("PROGRAMMED"));
        }
    }

    @Test // Request session with an valid underway state (Count 0)
    public void requestUnderwaySession() {
        String login = "testClient1@glue.gl";
        Result responseLogin = TestUtils.makeLoginRequest(login, login);

        String state = "underway";
        Result result = TestUtils.getSessionStateRequest(state, responseLogin);
        ObjectNode ret = TestUtils.toJson(result);

        assertNotNull(result);
        assertEquals(200, result.toScala().header().status());
        assertEquals(0, ret.get("sessions").size());
    }

    @Test // Request session with closed (closed and finished) state
    public void requestClosedSession() {
        String login = "testClient1@glue.gl";
        Result responseLogin = TestUtils.makeLoginRequest(login, login);

        String state = "closed";
        Result result = TestUtils.getSessionStateRequest(state, responseLogin);
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
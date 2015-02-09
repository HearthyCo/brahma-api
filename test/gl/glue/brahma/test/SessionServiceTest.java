package gl.glue.brahma.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gl.glue.brahma.model.sessionuser.SessionUser;
import gl.glue.brahma.service.SessionService;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class SessionServiceTest extends TransactionalTest {

    private SessionService sessionService = new SessionService();

    @Test // Request with invalid user Authentication. User "testClient2" is not an user for session 90700
    public void requestSessionWithInvalidAuthentication() {
        int session = 90700;
        int uid = 90001;
        ObjectNode result = sessionService.getSession(session, uid);
        assertNull(result);
    }

    @Test // Request with an non-existent session id
    public void requestSessionInvalidId() {
        int session = 0;
        int uid = 90000;
        ObjectNode result = sessionService.getSession(session, uid);
        assertNull(result);
    }

    @Test // Valid request
    public void requestSessionOk() {
        int session = 90700;
        int uid = 90000;
        ObjectNode result = sessionService.getSession(session, uid);
        assertNotNull(result);
        assertEquals(session, result.get("session").get("id").asInt());
    }

    @Test // Request with an invalid session state
    public void requestInvalidSessionState() {
        String state = "dummystate";
        int uid = 90000;
        List<SessionUser> result = sessionService.getState(state, uid);
        assertNull(result);
    }

    @Test // Request session with an valid programmed state
    public void requestProgrammedSession() {
        String state = "programmed";
        int uid = 90000;

        List<SessionUser> result = sessionService.getState(state, uid);

        assertNotNull(result);
        assertEquals(2, result.size());
        for (SessionUser session : result) {
            assertEquals("PROGRAMMED", session.getSession().getState().name());
        }
    }

    @Test // Request session with an valid underway state (Count 0)
    public void requestUnderwaySession() {
        String state = "underway";
        int uid = 90000;
        List<SessionUser> result = sessionService.getState(state, uid);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test // Request session with closed (closed and finished) state
    public void requestClosedSession() {
        String state = "closed";
        int uid = 90000;
        List<SessionUser> result = sessionService.getState(state, uid);
        assertNotNull(result);
        assertEquals(2, result.size());
        for (SessionUser session : result) {
            String stateName = session.getSession().getState().name();
            assertTrue(stateName.equals("CLOSED") || stateName.equals("FINISHED"));
        }
    }

    @Test // Request with invalid user Authentication. User "testClientDummy" not exists
    public void requestSessionsWithInvalidAuthentication() {
        int uid = 99999;
        ObjectNode result = sessionService.getUserSessions(uid);

        JsonNode programmed = result.get("programmed");
        JsonNode underway = result.get("underway");
        JsonNode closed = result.get("closed");

        assertEquals(0, programmed.size());
        assertEquals(0, underway.size());
        assertEquals(0, closed.size());
    }

    @Test // Request sessions with valid user Authentication
    public void requestSessionsOk() {
        int uid = 90000;
        ObjectNode result = sessionService.getUserSessions(uid);

        JsonNode programmed = result.get("programmed");
        assertEquals(2, programmed.size());
        assertEquals(90700, programmed.get(0).get("id").asInt());

        JsonNode underway = result.get("underway");
        assertEquals(0, underway.size());

        JsonNode closed = result.get("closed");
        assertEquals(2, closed.size());
        assertEquals(90702, closed.get(0).get("id").asInt());
    }
}

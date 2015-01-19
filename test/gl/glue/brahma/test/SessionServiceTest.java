package gl.glue.brahma.test;

import com.fasterxml.jackson.databind.node.ObjectNode;
import gl.glue.brahma.model.session.Session;
import gl.glue.brahma.service.SessionService;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class SessionServiceTest extends TransactionalTest {

    private SessionService sessionService = new SessionService();

    @Test // Request with invalid user Authentication. User "testClient2" is not an user for session 90700
    public void returnSessionWithInvalidAuthentication() {
        int session = 90700;
        String login = "testClient2";
        ObjectNode result = sessionService.getSession(session, login);
        assertEquals(null, result);
    }

    @Test // Request with an non-existent session id
    public void returnSessionInvalidId() {
        int session = 0;
        String login = "testClient1";
        ObjectNode result = sessionService.getSession(session, login);
        assertEquals(null, result);
    }

    @Test // Valid request
    public void returnSessionOk() {
        int session = 90700;
        String login = "testClient1";
        ObjectNode result = sessionService.getSession(session, login);
        assertNotNull(result);
        assertEquals(session, result.get("session").get("id").asInt());
    }

    @Test // Request with an invalid session state
    public void requestInvalidSessionState() {
        String state = "dummystate";
        String login = "testClient1";
        List<Session> result = sessionService.getState(state, login);
        assertEquals(null, result);
    }

    @Test // Request session with an valid programmed state
    public void requestProgrammedSession() {
        String state = "programmed";
        String login = "testClient1";
        List<Session> result = sessionService.getState(state, login);
        assertNotNull(result);
        assertEquals(1, result.size());
        for (Session session : result) {
            assertEquals(state, session.getState().toString().toLowerCase());
        }
    }

    @Test // Request session with an valid underway state (Count 0)
    public void requestUnderwaySession() {
        String state = "underway";
        String login = "testClient1";
        List<Session> result = sessionService.getState(state, login);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test // Request session with closed (closed and finished) state
    public void requestClosedSession() {
        String state = "closed";
        String login = "testClient1";
        List<Session> result = sessionService.getState(state, login);
        assertNotNull(result);
        assertEquals(2, result.size());
        for (Session session : result) {
            assertTrue(state.equals(session.getState().toString().toLowerCase()) ||
                    "finished".equals(session.getState().toString().toLowerCase()));
        }
    }

}

package gl.glue.brahma.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gl.glue.brahma.service.SessionService;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class SessionServiceTest extends TransactionalTest {

    private SessionService sessionService = new SessionService();

    @Test // Request with invalid user Authentication. User "testClient2" is not an user for session 90700
    public void returnSessionWithInvalidAuthentication() {
        int session = 90700;
        int uid = 90001;
        ObjectNode result = sessionService.getSession(session, uid);
        assertEquals(null, result);
    }

    @Test // Request with an non-existent session id
    public void returnSessionInvalidId() {
        int session = 0;
        int uid = 90000;
        ObjectNode result = sessionService.getSession(session, uid);
        assertEquals(null, result);
    }

    @Test // Valid request
    public void returnSessionOk() {
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
        List<ObjectNode> result = sessionService.getState(state, uid);
        assertEquals(null, result);
    }

    @Test // Request session with an valid programmed state
    public void requestProgrammedSession() {
        String state = "programmed";
        int uid = 90000;

        List<ObjectNode> result = sessionService.getState(state, uid);

        assertNotNull(result);
        assertEquals(1, result.size());
        for (JsonNode session : result) {
            assertEquals("programmed", session.get("state").asText());
        }
    }

    @Test // Request session with an valid underway state (Count 0)
    public void requestUnderwaySession() {
        String state = "underway";
        int uid = 90000;
        List<ObjectNode> result = sessionService.getState(state, uid);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test // Request session with closed (closed and finished) state
    public void requestClosedSession() {
        String state = "closed";
        int uid = 90000;
        List<ObjectNode> result = sessionService.getState(state, uid);
        assertNotNull(result);
        assertEquals(2, result.size());
        for (JsonNode session : result) {
            String stateName = session.get("state").asText();
            assertTrue(stateName.equals("closed") || stateName.equals("finished"));
        }
    }

}

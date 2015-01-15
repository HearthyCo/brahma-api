package gl.glue.brahma.test;

import gl.glue.brahma.model.session.Session;
import gl.glue.brahma.service.SessionService;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SessionServiceTest extends TransactionalTest {

    private SessionService sessionService = new SessionService();

    @Test // Request with invalid user Authentication. User "testClient2" is not an user for session 90700
    public void returnSessionWithInvalidAuthentication() {
        int session = 90700;
        String login = "testClient2";
        Session result = sessionService.getSession(session, login);
        assertEquals(null, result);
    }

    @Test // Request with an non-existent session id
    public void returnSessionInvalidId() {
        int session = 0;
        String login = "testClient1";
        Session result = sessionService.getSession(session, login);
        assertEquals(null, result);
    }

    @Test // Valid request
    public void returnSessionOk() {
        int session = 90700;
        String login = "testClient1";
        Session result = sessionService.getSession(session, login);
        assertNotNull(result);
        assertEquals(session, result.getId());
    }

    @Test // Request with an invalid session state
    public void requestInvalidSessionState() {

    }

    @Test // Request with an valid session state
    public void requestValidSessionState() {

    }

    @Test // Request session with closed (closed and finished) state
    public void requestClosedSession() {

    }

}

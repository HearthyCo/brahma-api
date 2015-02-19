package gl.glue.brahma.test.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import gl.glue.brahma.model.session.Session;
import gl.glue.brahma.model.sessionuser.SessionUser;
import gl.glue.brahma.service.SessionService;
import utils.TransactionalTest;
import org.junit.Test;
import play.db.jpa.JPA;
import play.libs.Json;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class SessionServiceTest extends TransactionalTest {

    private SessionService sessionService = new SessionService();

    @Test // Request with invalid user Authentication. User uid = 1 is an invalid user
    public void requestNewSessionWithInvalidAuthentication() {
        int uid = 1;
        int serviceType = 90302;
        Session result = sessionService.requestSession(uid, serviceType, Session.State.REQUESTED);
        assertNull(result);
    }

    @Test // Request with invalid serviceType. ServiceType = 1 is an invalid serviceType
    public void requestNewSessionWithInvalidServiceType() {
        int uid = 90001;
        int serviceType = 1;
        Session result = sessionService.requestSession(uid, serviceType, Session.State.REQUESTED);
        assertNull(result);
    }

    @Test // Request new session
    public void requestNewSession() {
        int uid = 90000;
        int serviceType = 90302;
        Session result = sessionService.requestSession(uid, serviceType, Session.State.REQUESTED);
        ObjectNode ret = (ObjectNode) Json.toJson(result);
        assertEquals(Session.State.REQUESTED, Session.State.valueOf(ret.get("state").asText()));

        Session session = sessionService.getById(uid, ret.get("id").asInt());
        assertNotNull(session);
    }

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
        List<Set<Session.State>> states = new ArrayList<>();

        states.add(EnumSet.of(Session.State.PROGRAMMED));
        states.add(EnumSet.of(Session.State.UNDERWAY));
        states.add(EnumSet.of(Session.State.CLOSED, Session.State.FINISHED));

        // Iterate State Session List Array
        for (Set<Session.State> state : states) {
            List<SessionUser> sessionUser = sessionService.getUserSessionsByState(uid, state);
            assertEquals(0, sessionUser.size());
        }
    }

    @Test // Request sessions with valid user Authentication
    public void requestSessionsOk() {
        int uid = 90000;

        List<SessionUser> sessionsUser;
        Set<Session.State> state;

        state = EnumSet.of(Session.State.PROGRAMMED);
        sessionsUser = sessionService.getUserSessionsByState(uid, state);
        assertEquals(2, sessionsUser.size());
        assertEquals(91600, sessionsUser.get(0).getId());

        state = EnumSet.of(Session.State.UNDERWAY);
        sessionsUser = sessionService.getUserSessionsByState(uid, state);
        assertEquals(0, sessionsUser.size());

        state = EnumSet.of(Session.State.CLOSED, Session.State.FINISHED);
        sessionsUser = sessionService.getUserSessionsByState(uid, state);
        assertEquals(2, sessionsUser.size());
        assertEquals(91604, sessionsUser.get(0).getId());
    }

    @Test
    public void assignSessionFromPool() {
        int uid = 90005;
        Session session = sessionService.assignSessionFromPool(uid, 90302);
        assertNotNull(session);
        assertEquals(Session.State.UNDERWAY, session.getState());

        List<SessionUser> userUnderwaySessions = sessionService.getState("underway", uid);
        List<SessionUser> matches = userUnderwaySessions
                .stream()
                .filter(su -> su.getSession().getId() == session.getId())
                .collect(Collectors.toList());
        assertEquals(1, matches.size());
    }

    @Test
    public void assignSessionFromPoolEmpty() {
        int uid = 90005;
        Session session = sessionService.assignSessionFromPool(uid, 90302);
        assertNotNull(session);
        JPA.em().flush();

        // There should be only one session in queue for this pool.
        Session session2 = sessionService.assignSessionFromPool(uid, 90302);
        assertNull(session2);
    }

    @Test
    public void getPoolsSize() {
        Map<Integer, Integer> pools = sessionService.getPoolsSize();
        assertNotNull(pools);
        assertEquals(1, pools.size());
        Map.Entry<Integer, Integer> first = pools.entrySet().iterator().next();
        assertEquals(90302, first.getKey().intValue());
        assertEquals(1, first.getValue().intValue());
    }

}
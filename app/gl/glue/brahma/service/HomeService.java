package gl.glue.brahma.service;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gl.glue.brahma.model.session.Session;
import gl.glue.brahma.model.session.SessionDao;
import gl.glue.brahma.model.sessionuser.SessionUser;
import play.db.jpa.Transactional;
import play.libs.Json;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class HomeService {

    public static int DEFAUL_LIMIT = 2;
    private SessionDao sessionDao = new SessionDao();

    /**
     * Return Sessions by user to show in home screen
     * @param uid User Login to search in DAO functions
     * @return ObjectNode with all sessions grouped by state.
     */
    @Transactional
    public ObjectNode getSessions(int uid) {
        // Create object for retun
        ObjectNode result = Json.newObject();

        // Create State Sesion List Array for iterate and pass DAO function a Session.State ArrayList
        List<Set<Session.State>> states = new ArrayList<>();

        String[] listStates = { "programmed", "underway", "closed" };

        states.add(EnumSet.of(Session.State.PROGRAMMED));
        states.add(EnumSet.of(Session.State.UNDERWAY));
        states.add(EnumSet.of(Session.State.CLOSED, Session.State.FINISHED));

        // Iterate State Sesion List Array
        for (Set<Session.State> state : states) {
            List<SessionUser> sessionUsers = sessionDao.findByState(state, uid, DEFAUL_LIMIT);

            ArrayNode sessions = new ArrayNode(JsonNodeFactory.instance);
            for(SessionUser sessionUser : sessionUsers) sessions.add(Json.toJson(sessionUser/*, alowed fields */));

            result.put(listStates[states.indexOf(state)], sessions);
        }

        return result;
    }
}

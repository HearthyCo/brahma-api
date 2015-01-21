package gl.glue.brahma.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import gl.glue.brahma.model.session.Session;
import gl.glue.brahma.model.session.SessionUtils;
import gl.glue.brahma.model.session.SessionDao;
import play.db.jpa.Transactional;
import play.libs.Json;

import java.util.ArrayList;
import java.util.List;

public class HomeService {

    public static int DEFAUL_LIMIT = 2;
    private SessionDao sessionDao = new SessionDao();

    /**
     * Return Sessions by user to show in home screen
     * @param login User Login to search in DAO functions
     * @return ObjectNode with all sessions grouped by state.
     */
    @Transactional
    public ObjectNode getSessions(String login) {
        // Create object for retun
        ObjectNode result = Json.newObject();

        // Create State Sesion List Array for iterate and pass DAO function a Session.State ArrayList
        ArrayList<List<Session.State>> states = new  ArrayList<List<Session.State>>(){};
        states.add(new ArrayList<Session.State>() {{ add(Session.State.PROGRAMMED); }});
        states.add(new ArrayList<Session.State>() {{ add(Session.State.UNDERWAY); }});
        states.add(new ArrayList<Session.State>() {{ add(Session.State.CLOSED); add(Session.State.FINISHED); }});

        // Iterate State Sesion List Array
        for (List<Session.State> state : states) {
            List<Object[]> sessionsDao = sessionDao.findByState(state, login, DEFAUL_LIMIT);

            ArrayList<ObjectNode> sessions = new ArrayList<ObjectNode>(){};
            for(Object[] session : sessionsDao) sessions.add(SessionUtils.createSessionObject(session));

            if ((sessions != null) && !sessions.isEmpty()) {
                String stateName = state.get(0).toString().toLowerCase();
                result.put(stateName, Json.toJson(sessions));
            }
        }

        if (result.size() > 0) {
            return result;
        }
        else {
            return null;
        }
    }
}

package gl.glue.brahma.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gl.glue.brahma.model.session.Session;
import gl.glue.brahma.model.session.SessionDao;
import play.db.jpa.Transactional;
import play.libs.Json;

import java.util.ArrayList;
import java.util.List;

public class HomeService {

    public static int DEFAUL_LIMIT = 2;
    private SessionDao sessionDao = new SessionDao();

    private ObjectNode createSessionObject(Object[] sessionObject) {
        JsonNode session = Json.toJson(sessionObject);
        ObjectNode result = Json.newObject();

        result.put("id", session.get(0).asText());
        result.put("title", session.get(1).asText());
        result.put("startDate", session.get(2));

        if (session.get(3) != null) {
            result.put("isNew", Boolean.parseBoolean(session.get(3).asText()));
        }
        else {
            result.put("isNew", false);
        }

        return result;
    }

    @Transactional
    public ObjectNode getSessions(String login) {

        ObjectNode result = Json.newObject();

        ArrayList<List<Session.State>> states = new  ArrayList<List<Session.State>>(){};

        states.add(new ArrayList<Session.State>() {{ add(Session.State.PROGRAMMED); }});
        states.add(new ArrayList<Session.State>() {{ add(Session.State.UNDERWAY); }});
        states.add(new ArrayList<Session.State>() {{ add(Session.State.CLOSED); add(Session.State.FINISHED); }});


        for (List<Session.State> state : states) {
            List<Object[]> sessionsDao = sessionDao.findByState(state, login, DEFAUL_LIMIT);
            ArrayList<ObjectNode> sessions = new ArrayList<ObjectNode>(){};

            for(Object[] session : sessionsDao) {
                sessions.add(createSessionObject(session));
            }

            if (sessionsDao != null) {
                result.put(state.get(0).toString().toLowerCase(), Json.toJson(sessions));
            }
        }

        return result;
    }
}

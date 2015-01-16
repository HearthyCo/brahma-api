package gl.glue.brahma.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import gl.glue.brahma.model.session.Session;
import gl.glue.brahma.model.session.SessionDao;
import gl.glue.brahma.model.sessionuser.SessionUser;
import play.db.jpa.Transactional;
import play.libs.Json;

import java.util.ArrayList;
import java.util.List;

public class SessionService {
    private SessionDao sessionDao = new SessionDao();

    @Transactional
    public ObjectNode getSession(int id, String login) {

        ObjectNode result = Json.newObject();

        Session session = sessionDao.findById(id, login);
        if (session != null) {
            result.put("session", Json.toJson(session));

            List<SessionUser> users = sessionDao.findUsers(id);
            if (users != null) {
                result.put("users", Json.toJson(users));
            }

            return result;
        } else {
            return null;
        }
    }

    @Transactional
    public List<Session> getState(String state, String login) {
        List<Session.State> states;

        switch (state) {
            case "programmed": states = new ArrayList<Session.State>(){{ add(Session.State.PROGRAMMED); }};
                break;
            case "underway": states = new ArrayList<Session.State>(){{ add(Session.State.UNDERWAY); }};
                break;
            case "closed": states = new ArrayList<Session.State>(){{ add(Session.State.CLOSED); add(Session.State.FINISHED); }};
                break;
            default: return null;
        }

        List<Session> session = sessionDao.findByState(states, login);
        if (session != null) {
            return session;
        } else {
            return null;
        }
    }

    public List<SessionUser> getUserSession(int id) {

        List<SessionUser> users = sessionDao.findUsers(id);
        if (users != null) {
            return users;
        } else {
            return null;
        }

    }
}

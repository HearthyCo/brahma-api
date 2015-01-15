package gl.glue.brahma.service;

import gl.glue.brahma.model.session.Session;
import gl.glue.brahma.model.session.SessionDao;
import play.db.jpa.Transactional;

import java.util.ArrayList;
import java.util.List;

public class SessionService {
    private SessionDao sessionDao = new SessionDao();

    @Transactional
    public Session getSession(int id, String login) {
        Session session = sessionDao.findById(id, login);
        if (session != null) {
            return session;
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
            default: states = new ArrayList<Session.State>(){};
                break;
        }

        List<Session> session = sessionDao.findByState(states, login);
        if (session != null) {
            return session;
        } else {
            return null;
        }
    }
}

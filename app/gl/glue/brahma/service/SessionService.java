package gl.glue.brahma.service;

import gl.glue.brahma.model.session.Session;
import gl.glue.brahma.model.session.SessionDao;
import play.db.jpa.Transactional;

public class SessionService {
    private SessionDao sessionDao = new SessionDao();

    @Transactional
    public Session get(int id, String login) {
        Session session = sessionDao.findById(id, login);
        if (session != null) {
            return session;
        } else {
            return null;
        }
    }
}

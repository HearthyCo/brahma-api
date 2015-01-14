package gl.glue.brahma.service;

import gl.glue.brahma.model.session.Session;
import gl.glue.brahma.model.session.SessionDao;
import play.db.jpa.Transactional;

public class SessionService {
    private SessionDao sessionDao = new SessionDao();

    @Transactional
    public Session get(int id) {
        Session session = sessionDao.findById(id);
        if (session != null) {
            return session;
        } else {
            return null;
        }
    }
}

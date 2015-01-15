package gl.glue.brahma.model.session;

import play.db.jpa.JPA;

import javax.persistence.NoResultException;
import java.util.List;

public class SessionDao {

    public Session findById(int id, String login) {
        try {
            String query =
                "select session from Session session, SessionUser sessionUser" +
                " where session.id = :id and sessionUser.session.id = session.id and sessionUser.user.login = :login";

            return JPA.em().createQuery(query, Session.class)
                    .setParameter("id", id)
                    .setParameter("login", login)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<Session> findByState(List<Session.State> states, String login) {
        try {
            String query =
                "select session from Session session, SessionUser sessionUser " +
                "where session.state in :states " +
                "and sessionUser.session.id = session.id and sessionUser.user.login = :login";

            return JPA.em().createQuery(query, Session.class)
                    .setParameter("states", states)
                    .setParameter("login", login)
                    .getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }
}

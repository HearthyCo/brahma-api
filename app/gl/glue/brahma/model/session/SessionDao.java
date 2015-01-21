package gl.glue.brahma.model.session;

import gl.glue.brahma.model.sessionuser.SessionUser;
import play.db.jpa.JPA;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.List;
import java.util.Set;

public class SessionDao {

    public void save(Session session) {
        JPA.em().persist(session);
        JPA.em().flush();
    }

    public Session findById(int id, int uid) {
        try {
            String query =
                "select session from Session session, SessionUser sessionUser" +
                " where session.id = :id and sessionUser.session.id = session.id and sessionUser.user.id = :uid";

            return JPA.em().createQuery(query, Session.class)
                    .setParameter("id", id)
                    .setParameter("uid", uid)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Search in database all sessions with state passed
     * @param states States allowed to search
     * @param uid User login to filter query
     * @param limit Number for limit query
     * @return List Object[] (id, title, startDate, isNew) with all sessions grouped by state.
     */
    public List<SessionUser> findByState(Set<Session.State> states, int uid, int limit) {
        try {
            // Select id, title, startDate, isNew
            String queryString =
                    "select sessionUser " +
                    "from SessionUser sessionUser " +
                    "left join fetch Session session " +
                    "where session.state in :states " +
                    "and sessionUser.session.id = session.id and sessionUser.user.id = :uid";

            Query queryListSessionsState = JPA.em().createQuery(queryString, SessionUser.class)
                    .setParameter("states", states)
                    .setParameter("uid", uid);

            // If limit is greater than 0, means that limit is not a default value, set max results
            if (limit > 0) {
                queryListSessionsState.setMaxResults(limit);
            }

            return queryListSessionsState.getResultList();

        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Method overloading findByState(List<Session.State> states, String login, int limit)
     * @param states States allowed to search
     * @param uid User login to filter query
     * @return findByState function with default limit
     */
    public List<SessionUser> findByState(Set<Session.State> states, int uid) {
        return findByState(states, uid, 0);
    }

    public List<SessionUser> findUsersSession(int id) {
        try {
            String query = "select sessionUser " +
                    "from SessionUser sessionUser " +
                    "left join fetch sessionUser.user " +
                    "left join fetch sessionUser.service service " +
                    "left join fetch service.serviceType serviceType " +
                    "left join fetch serviceType.field " +
                    "where sessionUser.session.id = :id";

            return JPA.em().createQuery(query, SessionUser.class)
                    .setParameter("id", id)
                    .getResultList();

        } catch (NoResultException e) {
            return null;
        }
    }
}

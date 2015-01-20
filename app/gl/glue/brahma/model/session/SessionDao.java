package gl.glue.brahma.model.session;

import play.db.jpa.JPA;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.Date;
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

    /**
     * Search in database all sessions with state passed
     * @param states States allowed to search
     * @param login User login to filter query
     * @param limit Number for limit query
     * @return List Object[] (id, title, startDate, isNew) with all sessions grouped by state.
     */
    public List<Object[]> findByState(List<Session.State> states, String login, int limit) {
        try {
            // Select id, title, startDate, isNew
            String queryString =
                    "select session.id, session.title, session.startDate, (session.timestamp > sessionUser.viewedDate), session.state " +
                    "from Session session, SessionUser sessionUser " +
                    "where session.state in :states " +
                    "and sessionUser.session.id = session.id and sessionUser.user.login = :login";

            Query queryListSessionsState = JPA.em().createQuery(queryString)
                    .setParameter("states", states)
                    .setParameter("login", login);

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
     * @param login User login to filter query
     * @return findByState function with default limit
     */
    public List<Object[]> findByState(List<Session.State> states, String login) {
        return findByState(states, login, 0);
    }

    public List<Object[]> findUsersSession(int id) {
        try {
            String query = "select user.login, user.name, user.surname1, user.surname2, user.avatar, field.name, type(user), sessionUser.report " +
                    "from SessionUser sessionUser " +
                    "left join sessionUser.user user " +
                    "left join sessionUser.service service " +
                    "left join service.serviceType serviceType " +
                    "left join serviceType.field field " +
                    "where sessionUser.session.id = :id";

            return JPA.em().createQuery(query)
                    .setParameter("id", id)
                    .getResultList();

        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Update session user viewed date
     * @param id Identifier session
     * @return Result of execute update (1, 0)
     */
    public int setSessionUserViewedDate(int id) {
        Date now = new Date();
        String queryUpdateViewDate = "update SessionUser sessionUser set sessionUser.viewedDate = :now where sessionUser.session.id = :id";

        return JPA.em().createQuery(queryUpdateViewDate)
                .setParameter("id", id)
                .setParameter("now", now)
                .executeUpdate();
    }
}

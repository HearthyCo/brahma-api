package gl.glue.brahma.model.session;

import gl.glue.brahma.model.servicetype.ServiceType;
import gl.glue.brahma.model.sessionuser.SessionUser;
import javafx.util.Pair;
import play.db.jpa.JPA;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SessionDao {

    /**
     * Adds a newly created Session to the persistence context.
     * @param session The newly created session.
     */
    public void create(Session session) {
        JPA.em().persist(session);
    }


    /**
     * Finds the session with the given id, if the user participates on it.
     * @param id The id of the session.
     * @param uid The id of the user.
     * @return The session if the user participates on it, or null otherwise.
     */
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
        // Select id, title, startDate, isNew
        String queryString =
                "select sessionUser " +
                "from SessionUser sessionUser " +
                "left join fetch sessionUser.session session " +
                "where session.state in :states " +
                "and sessionUser.user.id = :uid";

        Query queryListSessionsState = JPA.em().createQuery(queryString, SessionUser.class)
                .setParameter("states", states)
                .setParameter("uid", uid);

        // If limit is greater than 0, means that limit is not a default value, set max results
        if (limit > 0) {
            queryListSessionsState.setMaxResults(limit);
        }

        return queryListSessionsState.getResultList();
    }


    /**
     * Find the number of sessions in the specified state for a given user.
     * @param states States to include in the count.
     * @param uid Target user id.
     * @return The number of sessions found.
     */
    public int countByState(Set<Session.State> states, int uid) {
        String queryString =
                "select count(sessionUser) " +
                "from SessionUser sessionUser " +
                "where sessionUser.session.state in :states " +
                "and sessionUser.user.id = :uid";

        return JPA.em().createQuery(queryString, Long.class)
                .setParameter("states", states)
                .setParameter("uid", uid)
                .getSingleResult()
                .intValue();
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


    /**
     * Returns a list of SessionUsers for a given Session id, pre-fetching their associated
     * services, service types and fields.
     * @param id The session id.
     * @return The list of SessionUsers on the Session.
     */
    public List<SessionUser> findUsersSession(int id) {
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
    }


    /**
     * Returns a list of the available session pools and how many sessions they contain.
     * @return The list of pools with their sizes.
     */
    public List<Pair<ServiceType, Integer>> getPoolsSize() {
        String query = "select session.serviceType, count(session) " +
                "from Session session " +
                "where session.state = :state " +
                "group by session.serviceType";

        return JPA.em().createQuery(query, Object[].class)
                .setParameter("state", Session.State.REQUESTED)
                .getResultList()
                .stream()
                .map(o -> new Pair<>((ServiceType) o[0], (Integer)o[1]))
                .collect(Collectors.toList());
    }


    /**
     * Returns the first requested session waiting on the selected pool (or null if there isn't any).
     * @param service_type_id The target session pool.
     * @return The first session on the queue, or null.
     */
    public Session getFromPool(int service_type_id) {
        try {
            String query = "select session " +
                    "from Session session " +
                    "where session.state = :state " +
                    "and session.serviceType.id = :type " +
                    "order by session.startDate asc";

            return JPA.em().createQuery(query, Session.class)
                    .setParameter("state", Session.State.REQUESTED)
                    .setParameter("type", service_type_id)
                    .setMaxResults(1)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

}

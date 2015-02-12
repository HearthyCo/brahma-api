package gl.glue.brahma.model.session;

import gl.glue.brahma.model.sessionuser.SessionUser;
import play.db.jpa.JPA;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
            return JPA.em().createNamedQuery("Session.findById", Session.class)
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
        Query queryListSessionsState = JPA.em().createNamedQuery("Session.findByState", SessionUser.class)
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
        return JPA.em().createNamedQuery("Session.countByState", Long.class)
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
        return JPA.em().createNamedQuery("Session.findUsersSession", SessionUser.class)
                .setParameter("id", id)
                .getResultList();
    }


    /**
     * Returns a map of the non-empty session pools, with their queue length.
     * @return A map keyed by the pool ids, with their queue length as the value.
     */
    public Map<Integer, Integer> getPoolsSize() {
        Map<Integer, Integer> pools = new LinkedHashMap<>();
        JPA.em().createNamedQuery("Session.getPoolsSize", Object[].class)
                .setParameter("state", Session.State.REQUESTED)
                .getResultList()
                .forEach(o -> pools.put((Integer) o[0], ((Long) o[1]).intValue()));
        return pools;
    }


    /**
     * Returns the first requested session waiting on the selected pool (or null if there isn't any).
     * @param service_type_id The target session pool.
     * @return The first session on the queue, or null.
     */
    public Session getFromPool(int service_type_id) {
        try {
            return JPA.em().createNamedQuery("Session.getFromPool", Session.class)
                    .setParameter("state", Session.State.REQUESTED)
                    .setParameter("type", service_type_id)
                    .setMaxResults(1)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

}

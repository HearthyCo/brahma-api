package gl.glue.brahma.model.session;

import gl.glue.brahma.model.sessionuser.SessionUser;
import play.db.jpa.JPA;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.*;

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
     * Finds the session with the given id.
     * @param id The id of the session.
     * @return The session if it exists, or null otherwise.
     */
    public Session findById(int id) {
        return JPA.em().find(Session.class, id);
    }


    /**
     * Finds all the sessions of a user on some specified states, up to a maximum.
     * @param states Target states
     * @param uid Target user
     * @param limit Max number of results returned, or 0 for no limit.
     * @return List of the matching SessionUsers.
     */
    public List<SessionUser> findByState(Set<Session.State> states, int uid, int limit) {
        String query = "Session.findByStateSortTS";
        if (states.contains(Session.State.PROGRAMMED)) {
            query = "Session.findByStateSortStart";
        }
        Query queryListSessionsState = JPA.em().createNamedQuery(query, SessionUser.class)
                .setParameter("states", states)
                .setParameter("uid", uid);

        // If limit is greater than 0, means that limit is not a default value, set max results
        if (limit > 0) {
            queryListSessionsState.setMaxResults(limit);
        }

        return queryListSessionsState.getResultList();
    }

    /**
     * Find sessions by serviceType
     * @param states Set of session states with valid states
     * @param serviceTypeId Target ServiceType
     * @param uid Target user id.
     * @return List of SessionUser in state passed filtered by ServiceType for an user
     */
    public List<SessionUser> findByService(int uid, Set<Session.State> states, int serviceTypeId) {
        String query = "Session.findByServiceIdSortTS";

        Query queryListSessionsState = JPA.em().createNamedQuery(query, SessionUser.class)
                .setParameter("states", states)
                .setParameter("serviceTypeId", serviceTypeId)
                .setParameter("uid", uid);

        return queryListSessionsState.getResultList();
    }

    /**
     * Find the number of sessions in the specified state and service type for a given user.
     * @param uid Target user id.
     * @param states States to include in the count.
     * @param stid Service Type id to filter by.
     * @return The number of sessions found.
     */
    public int countByStateAndType(int uid, Set<Session.State> states, int stid) {
        return JPA.em().createNamedQuery("Session.countByStateAndType", Long.class)
                .setParameter("uid", uid)
                .setParameter("states", states)
                .setParameter("stid", stid)
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

    public List<Integer> findIdsByUser(int uid) {
        return JPA.em().createNamedQuery("Session.findIdsByUser", Integer.class)
                .setParameter("uid", uid)
                .getResultList();
    }

    /**
     * Returns a map of all the currently open sessions to their participants.
     */
    public Map<Integer, List<Integer>> getSessionsParticipants(Set<Session.State> states) {
        Map<Integer, List<Integer>> ret = new LinkedHashMap<>();
        List<Object[]> participations = JPA.em().createNamedQuery("Session.getSessionsParticipants", Object[].class)
                .setParameter("states", states)
                .getResultList();
        for (Object[] participation: participations) {
            int sessionId = (Integer)participation[0];
            int userId = (Integer)participation[1];
            if (!ret.containsKey(sessionId)) ret.put(sessionId, new ArrayList<>());
            ret.get(sessionId).add(userId);
        }
        return ret;
    }

    /**
     * Returns a list of participants of the specified session.
     */
    public List<Integer> getSessionParticipants(int sessionId) {
        return JPA.em().createNamedQuery("Session.getSessionParticipants", Integer.class)
                .setParameter("id", sessionId)
                .getResultList();
    }

}

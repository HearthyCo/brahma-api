package gl.glue.brahma.model.sessionuser;

import play.db.jpa.JPA;

import java.util.List;

public class SessionUserDao {

    public void create(SessionUser sessionUser) {
        JPA.em().persist(sessionUser);
    }

    /**
     * Finds the SessionUser with the given id.
     * @param id The id of the SessionUser.
     * @return The SessionUser if it exists, or null otherwise.
     */
    public SessionUser findById(int id) {
        return JPA.em().find(SessionUser.class, id);
    }

    /**
     * Finds the SessionUsers for the given session.
     * @param id The id of the Session.
     * @return The SessionUser list.
     */
    public List<SessionUser> findBySession(int id) {
        return JPA.em().createNamedQuery("SessionUser.findBySession", SessionUser.class)
                .setParameter("id", id)
                .getResultList();
    }

}

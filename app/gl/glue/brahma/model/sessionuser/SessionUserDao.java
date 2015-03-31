package gl.glue.brahma.model.sessionuser;

import play.db.jpa.JPA;

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

}

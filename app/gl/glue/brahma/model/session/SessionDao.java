package gl.glue.brahma.model.session;

import play.db.jpa.JPA;
import javax.persistence.NoResultException;

public class SessionDao {

    public Session findById(int id) {
        try {
            return JPA.em().createQuery("select x from Session x where x.id = :id", Session.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

}

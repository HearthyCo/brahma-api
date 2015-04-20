package gl.glue.brahma.model.availability;

import gl.glue.brahma.model.attachment.Attachment;
import gl.glue.brahma.model.session.Session;
import gl.glue.brahma.model.sessionuser.SessionUser;
import play.db.jpa.JPA;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class AvailabilityDao {

    /**
     * Adds a newly created Availability to the persistence context.
     * @param availability The newly created Availability.
     */
    public void create(Availability availability) {
        JPA.em().persist(availability);
    }

    /**
     * Find an attachment by its id.
     * @param id The id of the availability.
     * @return The availability, or null if not found.
     */
    public Availability findById(int id) {
        return JPA.em().find(Availability.class, id);
    }

    /**
     * Find all the availabilities of a specified user
     * @param uid Target user's ID.
     * @return List of all the user availabilities
     */
    public List<Availability> findByUser(int uid) {
        Query availabilities = JPA.em().createNamedQuery("Availability.findByUser", Availability.class)
                .setParameter("uid", uid);

        return availabilities.getResultList();
    }

}

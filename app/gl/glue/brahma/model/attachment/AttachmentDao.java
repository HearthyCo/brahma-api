package gl.glue.brahma.model.attachment;

import play.db.jpa.JPA;

import java.util.List;

public class AttachmentDao {

    /**
     * Find an attachment by its id.
     * @return The attachment, or null if not found.
     */
    public Attachment getById(int id) {
        return JPA.em().find(Attachment.class, id);
    }

    public List<Attachment> getBySession(int sid) {
        return JPA.em().createNamedQuery("Attachment.findBySession", Attachment.class)
                .setParameter("sid", sid)
                .getResultList();
    }

    /**
     * Add the specified attachment to the persistence context.
     */
    public void create(Attachment attachment) {
        JPA.em().persist(attachment);
    }


}

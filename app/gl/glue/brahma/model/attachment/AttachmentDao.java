package gl.glue.brahma.model.attachment;

import play.db.jpa.JPA;

public class AttachmentDao {

    /**
     * Find an attachment by its id.
     * @return The attachment, or null if not found.
     */
    public Attachment getById(int id) {
        return JPA.em().find(Attachment.class, id);
    }

    /**
     * Add the specified attachment to the persistence context.
     */
    public void create(Attachment attachment) {
        JPA.em().persist(attachment);
    }


}

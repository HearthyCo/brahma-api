package gl.glue.brahma.model.historyentry;

import play.db.jpa.JPA;
import java.util.List;

public class HistoryEntryDao {

    /**
     * Finds a HistoryEntry by its ID
     * @param id HistoryEntry ID
     * @return The specified HistoryEntry, or null if not found
     */
    public HistoryEntry findById(int id) {
        return JPA.em().find(HistoryEntry.class, id);
    }

    /**
     * Finds the HistoryEntries of a given user.
     * @param uid Target user ID
     * @return List of HistoryEntry matching the criteria
     */
    public List<HistoryEntry> findByUser(int uid) {
        return JPA.em().createNamedQuery("HistoryEntry.findByUser", HistoryEntry.class)
                .setParameter("uid", uid)
                .getResultList();
    }

    /**
     * Finds the HistoryEntries of a given user of a certain type.
     * @param uid Target user ID
     * @param type Type of HistoryEntry to search for
     * @return List of HistoryEntry matching the criteria
     */
    public List<HistoryEntry> findByUserAndType(int uid, String type) {
        return JPA.em().createNamedQuery("HistoryEntry.findByUserAndType", HistoryEntry.class)
                .setParameter("uid", uid)
                .setParameter("kind", type)
                .getResultList();
    }

    /**
     * Saves a new HistoryEntry and makes it managed.
     * @param entry The new entry
     */
    public void create(HistoryEntry entry) {
        JPA.em().persist(entry);
    }

    /**
     * Updates an existing HistoryEntry, and returns a managed copy of it.
     * @param entry The new entry
     * @return A managed copy of the received entry
     */
    public HistoryEntry update(HistoryEntry entry) {
        return JPA.em().merge(entry);
    }

}

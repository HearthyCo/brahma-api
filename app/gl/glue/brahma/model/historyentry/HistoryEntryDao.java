package gl.glue.brahma.model.historyentry;

import play.db.jpa.JPA;
import play.db.jpa.Transactional;

import javax.persistence.NoResultException;
import java.util.List;

public class HistoryEntryDao {

    /**
     * Finds a HistoryEntry by its ID
     * @param id HistoryEntry ID
     * @return The specified HistoryEntry, or null if not found
     */
    @Transactional
    public HistoryEntry findById(int id) {
        try {
            return JPA.em().find(HistoryEntry.class, id);
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Finds the HistoryEntries of a given user.
     * @param uid Target user ID
     * @return List of HistoryEntry matching the criteria
     */
    @Transactional
    public List<HistoryEntry> findByUser(int uid) {
        try {
            String queryString =
                    "select he " +
                            "from HistoryEntry he " +
                            "where he.owner.id = :uid";

            return JPA.em().createQuery(queryString, HistoryEntry.class)
                    .setParameter("uid", uid)
                    .getResultList();

        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Finds the HistoryEntries of a given user of a certain type.
     * @param uid Target user ID
     * @param type Type of HistoryEntry to search for
     * @return List of HistoryEntry matching the criteria
     */
    @Transactional
    public List<HistoryEntry> findByUserAndType(int uid, String type) {
        try {
            String queryString =
                    "select he " +
                    "from HistoryEntry he " +
                    "where he.owner.id = :uid " +
                    "and he.type = :kind";

            return JPA.em().createQuery(queryString, HistoryEntry.class)
                    .setParameter("uid", uid)
                    .setParameter("kind", type)
                    .getResultList();

        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Saves a new HistoryEntry and makes it managed.
     * @param entry The new entry
     */
    @Transactional
    public void create(HistoryEntry entry) {
        JPA.em().persist(entry);
    }

    /**
     * Updates an existing HistoryEntry, and returns a managed copy of it.
     * @param entry The new entry
     * @return A managed copy of the received entry
     */
    @Transactional
    public HistoryEntry update(HistoryEntry entry) {
        return JPA.em().merge(entry);
    }

}

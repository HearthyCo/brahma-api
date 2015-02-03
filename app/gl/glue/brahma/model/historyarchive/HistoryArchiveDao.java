package gl.glue.brahma.model.historyarchive;

import play.db.jpa.JPA;
import play.db.jpa.Transactional;

import javax.persistence.NoResultException;
import java.util.List;

public class HistoryArchiveDao {

    /**
     * Returns the full history for a given HistoryEntry id.
     * @param id Target user ID
     * @return List of HistoryArchive related to the entry
     */
    @Transactional
    public List<HistoryArchive> findByHistoryEntry(int id) {
        try {
            String queryString =
                    "select ha " +
                            "from HistoryArchive ha " +
                            "where ha.historyEntry.id = :id";

            return JPA.em().createQuery(queryString, HistoryArchive.class)
                    .setParameter("id", id)
                    .getResultList();

        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Saves a new HistoryArchive and makes it managed.
     * @param entry The new entry
     */
    @Transactional
    public void create(HistoryArchive entry) {
        JPA.em().persist(entry);
    }

}

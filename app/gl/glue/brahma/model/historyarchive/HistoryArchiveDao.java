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
        return JPA.em().createNamedQuery("HistoryArchive.findByHistoryEntry", HistoryArchive.class)
                .setParameter("id", id)
                .getResultList();
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

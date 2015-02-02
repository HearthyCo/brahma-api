package gl.glue.brahma.model.historyentry;

import gl.glue.brahma.model.service.Service;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;

import javax.persistence.NoResultException;
import java.util.List;

public class HistoryEntryDao {

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
                    "where he.id = :uid " +
                    "and he.type = :kind";

            return JPA.em().createQuery(queryString, HistoryEntry.class)
                    .setParameter("uid", uid)
                    .setParameter("kind", type)
                    .getResultList();

        } catch (NoResultException e) {
            return null;
        }
    }

}

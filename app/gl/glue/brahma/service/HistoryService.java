package gl.glue.brahma.service;

import gl.glue.brahma.model.historyarchive.HistoryArchive;
import gl.glue.brahma.model.historyarchive.HistoryArchiveDao;
import gl.glue.brahma.model.historyentry.HistoryEntry;
import gl.glue.brahma.model.historyentry.HistoryEntryDao;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;

import java.util.Date;
import java.util.List;

public class HistoryService {

    private HistoryEntryDao historyEntryDao = new HistoryEntryDao();
    private HistoryArchiveDao historyArchiveDao = new HistoryArchiveDao();

    /**
     * Finds the HistoryEntries of a given user.
     * @param uid Target user ID
     * @return List of HistoryEntry matching the criteria
     */
    @Transactional
    public List<HistoryEntry> getHistory(int uid) {
        return historyEntryDao.findByUser(uid);
    }

    /**
     * Finds the HistoryEntries of a given user of a certain type.
     * @param uid Target user ID
     * @param section Type of HistoryEntry to search for
     * @return List of HistoryEntry matching the criteria
     */
    @Transactional
    public List<HistoryEntry> getHistorySection(int uid, String section) {
        return historyEntryDao.findByUserAndType(uid, section);
    }

    /**
     * Finds the HistoryArchives of a given entry.
     * @param id Target entry ID
     * @return List of HistoryArchive for the entry
     */
    @Transactional
    public List<HistoryArchive> getEntryHistory(int id) {
        return historyArchiveDao.findByHistoryEntry(id);
    }

    /**
     * Creates or updates a HistoryEntry, properly keeping the change history.
     * This method expects an unmanaged entry (like those obtained from a Jackson conversion from JSON), and will
     * bail out if it receives a managed one, as it would be impossible to backup the original data without
     * leaving the transaction.
     * @param entry The new or updated entry
     */
    @Transactional
    public HistoryEntry saveVersioned(HistoryEntry entry) {
        // If object exists in persistence context, bail out
        if (JPA.em().contains(entry)) {
            throw new IllegalStateException("Non-managed object expected");
        }
        entry.setTimestamp(new Date());
        if (entry.getId() != 0) {
            // If object has ID, we're handling an update, and should archive the original values first
            HistoryEntry original = historyEntryDao.findById(entry.getId());
            HistoryArchive archive = HistoryArchive.fromHistoryEntry(original);
            historyArchiveDao.create(archive);
            return historyEntryDao.update(entry);
        } else {
            // We're handling a creation, so just persist it and we're done
            historyEntryDao.create(entry);
            return entry;
        }

    }


}

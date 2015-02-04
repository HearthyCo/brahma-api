package gl.glue.brahma.test;

import gl.glue.brahma.model.historyarchive.HistoryArchive;
import gl.glue.brahma.model.historyentry.HistoryEntry;
import gl.glue.brahma.model.historyentrytype.HistoryEntryType;
import gl.glue.brahma.model.user.Client;
import gl.glue.brahma.model.user.User;
import gl.glue.brahma.service.HistoryService;
import gl.glue.brahma.service.UserService;
import org.junit.Test;
import play.libs.Json;

import java.util.List;

import static org.junit.Assert.*;

public class HistoryServiceTest extends TransactionalTest {

    private HistoryService historyService = new HistoryService();
    private UserService userService = new UserService();

    @Test
    public void createNewEntry() {
        User user = userService.getById(90000);

        HistoryEntry he = new HistoryEntry();
        he.setOwner(user);
        he.setTitle("Test Title");
        he.setEditor(user);
        he.setMeta(Json.newObject());
        he.setType(new HistoryEntryType("allergies"));
        he = historyService.saveVersioned(he);

        List<HistoryEntry> lhe = historyService.getHistory(user.getId());
        assertTrue(lhe.contains(he));

        List<HistoryArchive> lha = historyService.getEntryHistory(he.getId());
        assertEquals(0, lha.size());
    }

    @Test
    public void updateEntry() {
        User user = userService.getById(90000);
        String title1 = "Test Title";
        String title2 = "Test Title 2";

        HistoryEntry he = new HistoryEntry();
        he.setOwner(user);
        he.setTitle(title1);
        he.setEditor(user);
        he.setMeta(Json.newObject());
        he.setType(new HistoryEntryType("allergies"));
        he = historyService.saveVersioned(he);

        HistoryEntry he2 = Json.fromJson(Json.toJson(he), HistoryEntry.class);
        he2.setTitle(title2);
        // Re-set these fields because they cannot be [de]serialized properly
        he2.setOwner(user);
        he2.setEditor(user);
        he2 = historyService.saveVersioned(he2);

        List<HistoryEntry> lhe = historyService.getHistory(user.getId());
        assertTrue(lhe.contains(he2));

        List<HistoryArchive> lha = historyService.getEntryHistory(he.getId());
        assertEquals(1, lha.size());
        assertEquals(title1, lha.get(0).getMeta().get("title").asText());
    }


}

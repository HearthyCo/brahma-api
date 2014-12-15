package gl.glue.brahma.model.attachment;

import gl.glue.brahma.model.historyentry.HistoryEntry;
import gl.glue.brahma.model.session.Session;
import gl.glue.brahma.model.user.User;

import javax.persistence.*;

@Entity
public class Attachment {

    @Id
    @SequenceGenerator(name = "attachment_id_seq", sequenceName = "attachment_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "attachment_id_seq")
    private int id;

    @ManyToOne
    @JoinColumn(name = "history_entry_id")
    private HistoryEntry historyEntry;

    @ManyToOne
    @JoinColumn(name = "session_id")
    private Session session;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String path;


    public int getId() {
        return id;
    }

    public HistoryEntry getHistoryEntry() {
        return historyEntry;
    }

    public void setHistoryEntry(HistoryEntry historyEntry) {
        this.historyEntry = historyEntry;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }


    @Override
    public String toString() {
        return "Attachment #" + id;
    }
}

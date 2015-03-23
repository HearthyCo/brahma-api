package gl.glue.brahma.model.attachment;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.BooleanSerializer;
import gl.glue.brahma.model.historyentry.HistoryEntry;
import gl.glue.brahma.model.session.Session;
import gl.glue.brahma.model.user.User;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
public class Attachment {

    @Id
    @SequenceGenerator(name = "attachment_id_seq", sequenceName = "attachment_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "attachment_id_seq")
    private int id;

    @ManyToOne
    @JoinColumn(name = "history_entry_id")
    @JsonIgnore
    private HistoryEntry historyEntry;

    @ManyToOne
    @JoinColumn(name = "session_id")
    @JsonIgnore
    private Session session;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @NotNull
    private String url;

    @NotNull
    private String filename;

    @NotNull
    private int size;

    private String mime;

    private boolean hasThumb;


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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getMime() {
        return mime;
    }

    public void setMime(String mime) {
        this.mime = mime;
    }

    @JsonProperty("hasThumb")
    public boolean hasThumb() {
        return hasThumb;
    }

    public void setHasThumb(boolean hasThumb) {
        this.hasThumb = hasThumb;
    }


    @Override
    public String toString() {
        return "Attachment #" + id;
    }
}

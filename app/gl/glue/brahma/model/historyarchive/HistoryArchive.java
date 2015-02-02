package gl.glue.brahma.model.historyarchive;

import com.fasterxml.jackson.annotation.JsonIgnore;
import gl.glue.brahma.model.historyentry.HistoryEntry;
import gl.glue.brahma.model.user.User;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
public class HistoryArchive {

    @Id
    @SequenceGenerator(name = "history_archive_id_seq", sequenceName = "history_archive_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "history_archive_id_seq")
    private int id;

    @ManyToOne
    @JoinColumn(name = "history_entry_id")
    @NotNull
    @JsonIgnore
    private HistoryEntry historyEntry;

    @ManyToOne
    @JoinColumn(name = "editor_user_id")
    @JsonIgnore
    private User editor;

    @NotNull
    private Date timestamp;

    @NotNull
    private String meta;


    public int getId() {
        return id;
    }

    public HistoryEntry getHistoryEntry() {
        return historyEntry;
    }

    public void setHistoryEntry(HistoryEntry historyEntry) {
        this.historyEntry = historyEntry;
    }

    public User getEditor() {
        return editor;
    }

    public void setEditor(User professional) {
        this.editor = professional;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date archiveDate) {
        this.timestamp = archiveDate;
    }

    public String getMeta() {
        return meta;
    }

    public void setMeta(String meta) {
        this.meta = meta;
    }


    @Override
    public String toString() {
        return "HistoryArchive #" + id;
    }
}

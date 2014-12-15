package gl.glue.brahma.model.historyentry;

import gl.glue.brahma.model.historycurrent.HistoryCurrent;
import gl.glue.brahma.model.historyentrytype.HistoryEntryType;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
public class HistoryEntry {

    @Id
    @SequenceGenerator(name="history_entry_id_seq", sequenceName="history_entry_id_seq", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator="history_entry_id_seq")
    private int id;

    @ManyToOne
    @JoinColumn(name="history_current_id")
    @NotNull
    private HistoryCurrent history;

    @ManyToOne
    @JoinColumn(name="history_entry_type_id")
    @NotNull
    private HistoryEntryType type;

    @NotNull
    private String title;

    @NotNull
    private Date timestamp;

    private String description;

    @NotNull  // Fake to prevent typing bug
    private String meta;


    public int getId() {
        return id;
    }

    public HistoryCurrent getHistory() {
        return history;
    }

    public void setHistory(HistoryCurrent history) {
        this.history = history;
    }

    public HistoryEntryType getType() {
        return type;
    }

    public void setType(HistoryEntryType type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMeta() {
        return meta;
    }

    public void setMeta(String meta) {
        this.meta = meta;
    }


    @Override
    public String toString() {
        return "HistoryEntry #" + id;
    }

}

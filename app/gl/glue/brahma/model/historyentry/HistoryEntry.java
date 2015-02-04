package gl.glue.brahma.model.historyentry;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import gl.glue.brahma.model.historyentrytype.HistoryEntryType;
import gl.glue.brahma.model.user.User;
import play.libs.Json;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
public class HistoryEntry {

    @Id
    @SequenceGenerator(name = "history_entry_id_seq", sequenceName = "history_entry_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "history_entry_id_seq")
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "history_entry_type_id")
    @NotNull
    @JsonSerialize(using = ToStringSerializer.class)
    private HistoryEntryType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id")
    @NotNull
    @JsonIgnore
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "editor_user_id")
    @NotNull
    @JsonIgnore
    private User editor;

    @NotNull
    private String title;

    @NotNull
    private Date timestamp;

    @NotNull
    private boolean removed;

    private String description;

    @NotNull  // Fake to prevent typing bug
    private String meta;

    @Transient
    private JsonNode metaParsed; // Cache for meta parsing


    public int getId() {
        return id;
    }

    public HistoryEntryType getType() {
        return type;
    }

    public void setType(HistoryEntryType type) {
        this.type = type;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public User getEditor() {
        return editor;
    }

    public void setEditor(User editor) {
        this.editor = editor;
    }

    public boolean isRemoved() {
        return removed;
    }

    public void setRemoved(boolean removed) {
        this.removed = removed;
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

    public JsonNode getMeta() {
        if (metaParsed == null) {
            metaParsed = meta == null ? Json.newObject() : Json.parse(meta);
        }
        return metaParsed;
    }

    public void setMeta(JsonNode meta) {
        this.metaParsed = meta;
        this.meta = meta == null ? "{}" : meta.toString();
    }



    @Override
    public String toString() {
        return "HistoryEntry #" + id;
    }

}

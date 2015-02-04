package gl.glue.brahma.model.historyentrytype;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class HistoryEntryType {

    @Id
    private String id;


    public String getId() {
        return id;
    }


    @Override
    public String toString() {
        return id;
    }

    public HistoryEntryType() {}
    public HistoryEntryType(String id) {
        this.id = id;
    }
}

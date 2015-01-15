package gl.glue.brahma.model.session;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
public class Session {

    public enum State {REQUESTED, PROGRAMMED, UNDERWAY, CLOSED, FINISHED, CANCELED}


    @Id
    @SequenceGenerator(name = "session_id_seq", sequenceName = "session_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "session_id_seq")
    private int id;

    @NotNull
    private String title;

    @NotNull
    private Date startDate;

    private Date endDate;

    @Enumerated(EnumType.STRING)
    @NotNull
    private State state;

    private String meta;

    private Date timestamp;


    public int getId() {
        return id;
    }

    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public String getMeta() {
        return meta;
    }

    public void setMeta(String meta) {
        this.meta = meta;
    }
    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }


    @Override
    public String toString() {
        return "Session #" + id;
    }

}

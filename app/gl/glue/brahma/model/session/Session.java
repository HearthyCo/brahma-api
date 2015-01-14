package gl.glue.brahma.model.session;

import gl.glue.brahma.model.availability.Availability;
import gl.glue.brahma.model.notification.Notification;
import gl.glue.brahma.model.service.Service;
import gl.glue.brahma.model.user.Client;
import gl.glue.brahma.model.user.Professional;

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

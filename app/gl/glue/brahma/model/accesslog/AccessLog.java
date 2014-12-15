package gl.glue.brahma.model.accesslog;

import gl.glue.brahma.model.historycurrent.HistoryCurrent;
import gl.glue.brahma.model.user.Professional;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
public class AccessLog {

    @Id
    @SequenceGenerator(name = "access_log_id_seq", sequenceName = "access_log_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "access_log_id_seq")
    private int id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @NotNull
    private Professional user;

    @ManyToOne
    @JoinColumn(name = "history_current_id")
    @NotNull
    private HistoryCurrent historyCurrent;

    @NotNull
    private Date timestamp;


    public int getId() {
        return id;
    }

    public Professional getUser() {
        return user;
    }

    public void setUser(Professional user) {
        this.user = user;
    }

    public HistoryCurrent getHistoryCurrent() {
        return historyCurrent;
    }

    public void setHistoryCurrent(HistoryCurrent historyCurrent) {
        this.historyCurrent = historyCurrent;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }


    @Override
    public String toString() {
        return "AccessLog #" + id;
    }

}

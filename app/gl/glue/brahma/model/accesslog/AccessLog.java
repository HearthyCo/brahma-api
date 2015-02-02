package gl.glue.brahma.model.accesslog;

import com.fasterxml.jackson.annotation.JsonIgnore;
import gl.glue.brahma.model.user.User;

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
    @JoinColumn(name = "owner_user_id")
    @NotNull
    @JsonIgnore
    private User owner;

    @ManyToOne
    @JoinColumn(name = "viewer_user_id")
    @NotNull
    @JsonIgnore
    private User viewer;

    @NotNull
    private Date timestamp;


    public int getId() {
        return id;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public User getViewer() {
        return viewer;
    }

    public void setViewer(User viewer) {
        this.viewer = viewer;
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

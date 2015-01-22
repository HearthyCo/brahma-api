package gl.glue.brahma.model.sessionlog;

import com.fasterxml.jackson.annotation.JsonIgnore;
import gl.glue.brahma.model.session.Session;
import gl.glue.brahma.model.user.User;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
public class SessionLog {

    public enum Action {ACCEPT, JOIN, CLOSE, FINISH, REJECT, AWAY, ABORT}


    @Id
    @SequenceGenerator(name = "session_log_id_seq", sequenceName = "session_log_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "session_log_id_seq")
    private int id;

    @ManyToOne
    @JoinColumn(name = "session_id")
    @NotNull
    @JsonIgnore
    private Session session;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @NotNull
    @JsonIgnore
    private User user;

    @NotNull
    private Date timestamp;

    @Enumerated(EnumType.STRING)
    @NotNull
    private Action action;


    public int getId() {
        return id;
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

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }


    @Override
    public String toString() {
        return "SessionLog #" + id;
    }

}

package gl.glue.brahma.model.sessionuser;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import gl.glue.brahma.model.availability.Availability;
import gl.glue.brahma.model.notification.Notification;
import gl.glue.brahma.model.service.Service;
import gl.glue.brahma.model.session.Session;
import gl.glue.brahma.model.user.User;
import play.libs.Json;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
public class SessionUser {

    @Id
    @SequenceGenerator(name = "session_user_id_seq", sequenceName = "session_user_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "session_user_id_seq")
    private int id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @NotNull
    @JsonIgnore
    private User user;

    @ManyToOne
    @JoinColumn(name = "session_id")
    @NotNull
    @JsonIgnore
    private Session session;

    @JoinColumn(name = "viewed_date")
    private Date viewedDate;

    @OneToOne
    @JoinColumn(name = "notification_id")
    @JsonIgnore
    private Notification notification;

    @ManyToOne
    @JoinColumn(name = "service_id")
    @JsonIgnore
    private Service service;

    @ManyToOne
    @JoinColumn(name = "availability_id")
    @JsonIgnore
    private Availability availability;

    @NotNull
    private String meta;

    @Transient
    private JsonNode metaParsed; // Cache for meta parsing

    private String report;


    public int getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public Date getViewedDate() {
        return viewedDate;
    }

    public void setViewedDate(Date viewedDate) {
        this.viewedDate = viewedDate;
    }

    public Notification getNotification() {
        return notification;
    }

    public void setNotification(Notification notification) {
        this.notification = notification;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public Availability getAvailability() {
        return availability;
    }

    public void setAvailability(Availability availability) {
        this.availability = availability;
    }

    public JsonNode getMeta(){
        if (metaParsed == null) {
            metaParsed = meta == null ? Json.newObject() : Json.parse(meta);
        }
        return metaParsed;
    }

    public void setMeta(JsonNode meta) {
        this.metaParsed = meta;
        this.meta = meta == null ? "{}" : meta.toString();
    }

    public String getReport() {
        return report;
    }

    public void setReport(String report) {
        this.report = report;
    }

    @Override
    public String toString() {
        return "SessionUser #" + id;
    }

}

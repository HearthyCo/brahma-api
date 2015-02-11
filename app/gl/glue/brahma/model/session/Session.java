package gl.glue.brahma.model.session;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import gl.glue.brahma.model.field.Field;
import gl.glue.brahma.model.servicetype.ServiceType;
import play.libs.Json;

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

    @ManyToOne
    @JoinColumn(name = "service_type_id")
    @NotNull
    @JsonIgnore
    private ServiceType serviceType;

    @NotNull
    private String title;

    @NotNull
    private Date startDate;

    private Date endDate;

    @Enumerated(EnumType.STRING)
    @NotNull
    private State state;

    @NotNull // Fake to prevent typing bug
    private String meta = "{}";

    @Transient
    private JsonNode metaParsed; // Cache for meta parsing

    private Date timestamp;


    public int getId() {
        return id;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
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

package gl.glue.brahma.model.session;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import gl.glue.brahma.model.servicetype.ServiceType;
import gl.glue.brahma.util.serializers.ServiceTypeToIdSerializer;
import play.libs.Json;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@NamedQueries({

        @NamedQuery(
                name = "Session.findById",
                query = "select sessionUser.session " +
                        "from SessionUser sessionUser " +
                        "where sessionUser.session.id = :id " +
                        "and sessionUser.user.id = :uid"
        ),
        @NamedQuery(
                name = "Session.findIdsByUser",
                query = "select sessionUser.session.id " +
                        "from SessionUser sessionUser " +
                        "where sessionUser.user.id = :uid " +
                        "order by sessionUser.session.id asc"
        ),
        @NamedQuery(
                name = "Session.findByStateSortStart",
                query = "select sessionUser " +
                        "from SessionUser sessionUser " +
                        "left join fetch sessionUser.session session " +
                        "where session.state in :states " +
                        "and sessionUser.user.id = :uid " +
                        "order by session.startDate asc"
        ),
        @NamedQuery(
                name = "Session.findByStateSortTS",
                query = "select sessionUser " +
                        "from SessionUser sessionUser " +
                        "left join fetch sessionUser.session session " +
                        "where session.state in :states " +
                        "and sessionUser.user.id = :uid " +
                        "order by session.timestamp desc"
        ),
        @NamedQuery(
                name = "Session.countByState",
                query = "select count(sessionUser) " +
                        "from SessionUser sessionUser " +
                        "where sessionUser.session.state in :states " +
                        "and sessionUser.user.id = :uid"
        ),
        @NamedQuery(
                name = "Session.findUsersSession",
                query = "select sessionUser " +
                        "from SessionUser sessionUser " +
                        "left join fetch sessionUser.user " +
                        "left join fetch sessionUser.service service " +
                        "left join fetch service.serviceType serviceType " +
                        "left join fetch serviceType.field " +
                        "where sessionUser.session.id = :id"
        ),
        @NamedQuery(
                name = "Session.getPoolsSize",
                query = "select session.serviceType.id, count(session.id) " +
                        "from Session session " +
                        "where session.state = :state " +
                        "group by session.serviceType.id " +
                        "order by session.serviceType.id"
        ),
        @NamedQuery(
                name = "Session.getFromPool",
                query = "select session " +
                        "from Session session " +
                        "where session.state = :state " +
                        "and session.serviceType.id = :type " +
                        "order by session.startDate asc"
        ),
        @NamedQuery(
                name = "Session.findByServiceIdSortTS",
                query = "select sessionUser " +
                        "from SessionUser sessionUser " +
                        "left join fetch sessionUser.session session " +
                        "left join fetch sessionUser.service service " +
                        "where session.state in :states " +
                        "and sessionUser.user.id = :uid " +
                        "and service.serviceType.id = :serviceTypeId " +
                        "order by session.timestamp desc"
        )


})
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
    //@JsonIgnore
    @JsonSerialize(using = ServiceTypeToIdSerializer.class)
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

    @NotNull
    private Date timestamp;

    // Constructor
    public Session () {

    }

    public Session (ServiceType serviceType, String title, Date startDate, State state) {
        this.serviceType = serviceType;
        this.title = title;
        this.startDate = startDate;
        this.state = state;
        this.timestamp = new Date();
    }

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

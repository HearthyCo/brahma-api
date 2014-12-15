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

    @ManyToOne
    @JoinColumn(name = "client_user_id")
    @NotNull
    private Client client;

    @OneToOne
    @JoinColumn(name = "client_notification_id")
    private Notification clientNotification;

    @ManyToOne
    @JoinColumn(name = "professional_user_id")
    @NotNull
    private Professional professional;

    @OneToOne
    @JoinColumn(name = "professional_notification_id")
    private Notification professionalNotification;

    @ManyToOne
    @JoinColumn(name = "service_id")
    @NotNull
    private Service service;

    @ManyToOne
    @JoinColumn(name = "availability_id")
    @NotNull
    private Availability availability;

    @NotNull
    private Date startDate;

    private Date endDate;

    @Enumerated(EnumType.STRING)
    @NotNull
    private State state;

    private String report;

    private String meta;


    public int getId() {
        return id;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Notification getClientNotification() {
        return clientNotification;
    }

    public void setClientNotification(Notification clientNotification) {
        this.clientNotification = clientNotification;
    }

    public Professional getProfessional() {
        return professional;
    }

    public void setProfessional(Professional professional) {
        this.professional = professional;
    }

    public Notification getProfessionalNotification() {
        return professionalNotification;
    }

    public void setProfessionalNotification(Notification professionalNotification) {
        this.professionalNotification = professionalNotification;
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

    public String getReport() {
        return report;
    }

    public void setReport(String report) {
        this.report = report;
    }

    public String getMeta() {
        return meta;
    }

    public void setMeta(String meta) {
        this.meta = meta;
    }


    @Override
    public String toString() {
        return "Session #" + id;
    }

}

package gl.glue.brahma.model.historycurrent;

import gl.glue.brahma.model.user.Client;
import gl.glue.brahma.model.user.Professional;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
public class HistoryCurrent {

    @Id
    @SequenceGenerator(name="history_current_id_seq", sequenceName="history_current_id_seq", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator="history_current_id_seq")
    private int id;

    @ManyToOne
    @JoinColumn(name="client_user_id")
    @NotNull
    private Client client;

    @ManyToOne
    @JoinColumn(name="professional_user_id")
    private Professional professional;

    @NotNull
    private Date modificationDate;


    public int getId() {
        return id;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Professional getProfessional() {
        return professional;
    }

    public void setProfessional(Professional professional) {
        this.professional = professional;
    }

    public Date getModificationDate() {
        return modificationDate;
    }

    public void setModificationDate(Date modificationDate) {
        this.modificationDate = modificationDate;
    }


    @Override
    public String toString() {
        return "HistoryCurrent #" + id;
    }

}

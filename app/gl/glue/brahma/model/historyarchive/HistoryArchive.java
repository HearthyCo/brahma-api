package gl.glue.brahma.model.historyarchive;

import gl.glue.brahma.model.user.Client;
import gl.glue.brahma.model.user.Professional;
import gl.glue.brahma.model.user.User;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
public class HistoryArchive {

    @Id
    @SequenceGenerator(name = "history_archive_id_seq", sequenceName = "history_archive_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "history_archive_id_seq")
    private int id;

    @ManyToOne
    @JoinColumn(name = "client_user_id")
    @NotNull
    private Client client;

    @ManyToOne
    @JoinColumn(name = "professional_user_id")
    private Professional professional;

    @NotNull
    private Date creationDate;

    @NotNull
    private Date archiveDate;

    @NotNull
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

    public Professional getProfessional() {
        return professional;
    }

    public void setProfessional(Professional professional) {
        this.professional = professional;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getArchiveDate() {
        return archiveDate;
    }

    public void setArchiveDate(Date archiveDate) {
        this.archiveDate = archiveDate;
    }

    public String getMeta() {
        return meta;
    }

    public void setMeta(String meta) {
        this.meta = meta;
    }


    @Override
    public String toString() {
        return "HistoryArchive #" + id;
    }
}

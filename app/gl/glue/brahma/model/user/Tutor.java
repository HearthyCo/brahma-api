package gl.glue.brahma.model.user;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("TUTOR")
public class Tutor extends User {

    /*@OneToMany(mappedBy = "tutor")
    private Set<Client> clients;


    public Set<Client> getClients() {
        return clients;
    }*/

}

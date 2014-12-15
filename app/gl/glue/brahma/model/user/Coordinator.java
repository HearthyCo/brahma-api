package gl.glue.brahma.model.user;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.Set;

@Entity
@DiscriminatorValue("COORDINATOR")
public class Coordinator extends User {

    @OneToMany(mappedBy = "manager")
    private Set<Professional> professionals;


    public Set<Professional> getProfessionals() {
        return professionals;
    }

}

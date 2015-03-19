package gl.glue.brahma.model.user;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.Set;

@Entity
@DiscriminatorValue("COORDINATOR")
public class Coordinator extends User {

    @OneToMany(mappedBy = "manager")
    @JsonIgnore
    private Set<Professional> professionals;


    public Set<Professional> getProfessionals() {
        return professionals;
    }

    @Override
    public String getUserType() {
        return "coordinator";
    }
}

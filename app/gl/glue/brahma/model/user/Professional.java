package gl.glue.brahma.model.user;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity

@DiscriminatorValue("PROFESSIONAL")
public class Professional extends User {

    @ManyToOne
    @JoinColumn(name="manager_user_id")
    private Coordinator manager;

    public Coordinator getManager() {
        return manager;
    }

    public void setManager(Coordinator manager) {
        this.manager = manager;
    }
}

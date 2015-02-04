package gl.glue.brahma.model.user;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

@Entity

@DiscriminatorValue("PROFESSIONAL")
public class Professional extends User {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_user_id")
    @JsonIgnore
    private Coordinator manager;


    public Coordinator getManager() {
        return manager;
    }

    public void setManager(Coordinator manager) {
        this.manager = manager;
    }
}

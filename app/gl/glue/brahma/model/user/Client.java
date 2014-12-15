package gl.glue.brahma.model.user;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
@DiscriminatorValue("CLIENT")
public class Client extends User {

    @ManyToOne
    @JoinColumn(name = "tutor_user_id")
    private Tutor tutor;


    public Tutor getTutor() {
        return tutor;
    }

    public void setTutor(Tutor tutor) {
        this.tutor = tutor;
    }
}

package gl.glue.brahma.model.user;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

@Entity
@DiscriminatorValue("CLIENT")
public class Client extends User {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_user_id")
    @JsonIgnore
    private Tutor tutor;


    public Tutor getTutor() {
        return tutor;
    }

    public void setTutor(Tutor tutor) {
        this.tutor = tutor;
    }

    @Override
    public UserType getUserType() {
        return UserType.CLIENT;
    }
}

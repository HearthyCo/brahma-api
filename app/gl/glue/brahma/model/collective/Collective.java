package gl.glue.brahma.model.collective;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
public class Collective {

    @Id
    @SequenceGenerator(name = "collective_id_seq", sequenceName = "collective_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "collective_id_seq")
    private int id;

    @NotNull
    private String name;


    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    @Override
    public String toString() {
        return getName();
    }
}

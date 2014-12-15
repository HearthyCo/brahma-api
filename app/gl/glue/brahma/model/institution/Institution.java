package gl.glue.brahma.model.institution;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
public class Institution {

    @Id
    @SequenceGenerator(name = "institution_id_seq", sequenceName = "institution_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "institution_id_seq")
    private int id;

    @NotNull
    private String name;

    @NotNull
    private String meta;


    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMeta() {
        return meta;
    }

    public void setMeta(String meta) {
        this.meta = meta;
    }


    @Override
    public String toString() {
        return getName();
    }
}

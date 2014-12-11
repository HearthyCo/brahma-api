package model;

import javax.persistence.*;

@Entity
public class Colective {

    @Id
    @SequenceGenerator(name="colective_id_seq", sequenceName="colective_id_seq", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator="colective_id_seq")
    private int id;

    private String name;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

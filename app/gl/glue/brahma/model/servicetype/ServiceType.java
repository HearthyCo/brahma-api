package gl.glue.brahma.model.servicetype;

import gl.glue.brahma.model.field.Field;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
public class ServiceType {

    public enum ServiceMode {ASYNC, VIDEO}

    @Id
    @SequenceGenerator(name = "service_type_id_seq", sequenceName = "service_type_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "service_type_id_seq")
    private int id;

    @ManyToOne
    @JoinColumn(name = "field_id")
    @NotNull
    private Field field;

    @NotNull
    private int price;

    @NotNull
    private String name;

    @Enumerated(EnumType.STRING)
    @NotNull
    private ServiceMode mode;


    public int getId() {
        return id;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ServiceMode getMode() {
        return mode;
    }

    public void setMode(ServiceMode mode) {
        this.mode = mode;
    }


    @Override
    public String toString() {
        return getName();
    }
}

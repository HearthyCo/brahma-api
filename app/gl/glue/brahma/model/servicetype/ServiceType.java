package gl.glue.brahma.model.servicetype;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import gl.glue.brahma.model.field.Field;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@NamedQueries({

        @NamedQuery(
                name = "ServiceType.findById",
                query = "select serviceType " +
                        "from ServiceType serviceType " +
                        "left join fetch serviceType.field " +
                        "where serviceType.id = :id"
        ),
        @NamedQuery(
                name = "ServiceType.findServiceTypes",
                query = "select serviceType " +
                        "from ServiceType serviceType " +
                        "left join fetch serviceType.field"
        ),
        @NamedQuery(
                name = "ServiceType.findServiceTypesByField",
                query = "select serviceType " +
                        "from ServiceType serviceType " +
                        "left join fetch serviceType.field field " +
                        "where field.id = :fid"
        )

})
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
    @JsonSerialize(using = ToStringSerializer.class)
    private Field field;

    @NotNull
    private int price;

    @NotNull
    private String name;

    @Enumerated(EnumType.STRING)
    @NotNull
    private ServiceMode mode;

    private Integer poolsize;

    private Integer userlimit;


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

    public Integer getPoolsize() {
        return poolsize;
    }

    public void setPoolsize(Integer poolsize) {
        this.poolsize = poolsize;
    }

    public Integer getUserlimit() {
        return userlimit;
    }

    public void setUserlimit(Integer userlimit) {
        this.userlimit = userlimit;
    }


    @Override
    public String toString() {
        return getName();
    }
}

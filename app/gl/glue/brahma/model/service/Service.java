package gl.glue.brahma.model.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import gl.glue.brahma.model.servicetype.ServiceType;
import gl.glue.brahma.model.user.Professional;
import gl.glue.brahma.model.user.User;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
public class Service {

    @Id
    @SequenceGenerator(name = "service_id_seq", sequenceName = "service_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "service_id_seq")
    private int id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @NotNull
    @JsonIgnore
    private Professional provider;

    @ManyToOne
    @JoinColumn(name = "service_type_id")
    @NotNull
    @JsonIgnore
    private ServiceType serviceType;

    @NotNull
    private int earnings;


    public int getId() {
        return id;
    }

    public Professional getProvider() {
        return provider;
    }

    public void setProvider(Professional provider) {
        this.provider = provider;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
    }

    public int getEarnings() {
        return earnings;
    }

    public void setEarnings(int earnings) {
        this.earnings = earnings;
    }


    @Override
    public String toString() {
        return "Service #" + id;
    }
}

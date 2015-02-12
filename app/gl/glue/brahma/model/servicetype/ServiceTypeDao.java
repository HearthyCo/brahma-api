package gl.glue.brahma.model.servicetype;

import play.db.jpa.JPA;
import play.db.jpa.Transactional;

import javax.persistence.NoResultException;
import java.util.List;

public class ServiceTypeDao {

    /**
     * Find serviceType in database fetch serviceTypes
     * @return ServiceType
     */
    @Transactional
    public ServiceType findById(int id) {
        try {
            return JPA.em().createNamedQuery("ServiceType.findById", ServiceType.class)
                    .setParameter("id", id)
                    .getSingleResult();

        } catch (NoResultException e) {
            return null;
        }
    }


    /**
     * Find services in database fetch serviceTypes
     * @return List of services
     */
    @Transactional
    public List<ServiceType> findServiceTypes() {
        return JPA.em().createNamedQuery("ServiceType.findServiceTypes", ServiceType.class)
                .getResultList();
    }

    /**
     * Find services in database fetch serviceTypes
     * @return List of services
     */
    @Transactional
    public List<ServiceType> findServiceTypesByField(int fid) {
        return JPA.em().createNamedQuery("ServiceType.findServiceTypesByField", ServiceType.class)
                .setParameter("fid", fid)
                .getResultList();
    }

}

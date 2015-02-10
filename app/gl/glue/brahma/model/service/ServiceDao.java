package gl.glue.brahma.model.service;

import gl.glue.brahma.model.servicetype.ServiceType;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;

import javax.persistence.NoResultException;
import java.util.List;

public class ServiceDao {

    /**
     * Find services in database fetch serviceTypes
     * @return List of services
     */
    @Transactional
    public List<ServiceType> findServiceTypes() {
        try {
            String queryString =
                    "select serviceType " +
                    "from ServiceType serviceType " +
                    "left join fetch serviceType.field";

            return JPA.em().createQuery(queryString, ServiceType.class)
                    .getResultList();

        } catch (NoResultException e) {
            return null;
        }
    }
}

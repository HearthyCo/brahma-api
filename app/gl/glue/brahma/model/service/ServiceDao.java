package gl.glue.brahma.model.service;

import play.db.jpa.JPA;
import play.db.jpa.Transactional;

import javax.persistence.NoResultException;
import java.util.List;

public class ServiceDao {

    /**
     * Find services in database fetch serviceTypes
     * @return Lsit of services
     */
    @Transactional
    public List<Service> findServices() {
        try {
            String queryString =
                    "select service " +
                    "from Service service " +
                    "left join fetch service.serviceType";

            return JPA.em().createQuery(queryString, Service.class)
                    .getResultList();

        } catch (NoResultException e) {
            return null;
        }
    }
}

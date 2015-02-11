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
            String queryString =
                    "select serviceType " +
                            "from ServiceType serviceType " +
                            "left join fetch serviceType.field " +
                            "where serviceType.id = :id";

            return JPA.em().createQuery(queryString, ServiceType.class)
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

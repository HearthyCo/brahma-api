package gl.glue.brahma.model.service;

import play.db.jpa.JPA;
import play.db.jpa.Transactional;

import javax.persistence.NoResultException;

public class ServiceDao {

    /**
     * Find a suitable service for the given user and service type.
     * @return List of services
     */
    @Transactional
    public Service getServiceForType(int uid, int service_type_id) {
        try {
            String queryString =
                    "select service " +
                    "from Service service " +
                    "where service.provider.id = :uid " +
                    "and service.serviceType.id = :type";

            return JPA.em().createQuery(queryString, Service.class)
                    .setParameter("uid", uid)
                    .setParameter("type", service_type_id)
                    .setMaxResults(1)
                    .getSingleResult();

        } catch (NoResultException e) {
            return null;
        }
    }

}

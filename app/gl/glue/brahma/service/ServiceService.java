package gl.glue.brahma.service;

import gl.glue.brahma.model.service.Service;
import gl.glue.brahma.model.service.ServiceDao;
import gl.glue.brahma.model.servicetype.ServiceType;
import gl.glue.brahma.model.servicetype.ServiceTypeDao;
import play.db.jpa.Transactional;

import java.util.List;

public class ServiceService {

    private ServiceTypeDao serviceTypeDao = new ServiceTypeDao();
    private ServiceDao serviceDao = new ServiceDao();

    /**
     * Searches ServiceTypes by id.
     * @param sid The field id to filter by.
     * @return A list of ServiceTypes of the given field.
     */
    @Transactional
    public ServiceType findByTypeId(int sid) {
        return serviceTypeDao.findById(sid);
    }

    /**
     * Searches ServiceTypes by field.
     * @param fid The field id to filter by.
     * @return A list of ServiceTypes of the given field.
     */
    @Transactional
    public List<ServiceType> getServiceTypesByField(int fid) {
        return serviceTypeDao.findServiceTypesByField(fid);
    }

    /**
     * Finds all the ServiceTypes.
     * @return A list of all the ServiceTypes.
     */
    @Transactional
    public List<ServiceType> getAllServiceTypes() {
        return serviceTypeDao.findServiceTypes();
    }

    /**
     * Finds all the ServiceTypes.
     * @return A list of all the ServiceTypes.
     */
    @Transactional
    public List<Service> getServicesOfUser(int uid) {
        return serviceDao.getServicesOfUser(uid);
    }

}

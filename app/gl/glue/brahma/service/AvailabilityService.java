package gl.glue.brahma.service;

import gl.glue.brahma.model.availability.Availability;
import gl.glue.brahma.model.availability.AvailabilityDao;

import java.util.Date;
import java.util.List;

public class AvailabilityService {

    private AvailabilityDao availabilityDao = new AvailabilityDao();

    public Availability getById(int id) {
        return availabilityDao.findById(id);
    }

    public List<Availability> getByUser(int uid) {
        return availabilityDao.findByUser(uid);
    }

    public void create(Availability availability) {
        availabilityDao.create(availability);
    }

}

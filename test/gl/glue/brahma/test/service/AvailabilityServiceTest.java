package gl.glue.brahma.test.service;

import gl.glue.brahma.model.availability.Availability;
import gl.glue.brahma.model.user.Professional;
import gl.glue.brahma.service.AvailabilityService;
import gl.glue.brahma.service.UserService;
import org.junit.Test;
import utils.TransactionalTest;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AvailabilityServiceTest extends TransactionalTest {

    private AvailabilityService availabilityService = new AvailabilityService();
    private UserService userService = new UserService();


    @Test
    public void getAvailabilityByIdOk() {
        Availability availability = availabilityService.getById(90500);
        assertNotNull(availability);
        assertEquals(90005, availability.getUser().getId());
    }

    @Test
    public void getAvailabilitiesByUserOk() {
        List<Availability> availabilities = availabilityService.getByUser(90005);
        assertEquals(2, availabilities.size());
        assertEquals(90500, availabilities.get(0).getId());
    }

    @Test
    public void createAvailabilityOk() {
        int uid = 90006;
        Professional user = (Professional)userService.getById(uid);
        Availability availability = new Availability(user);
        availability.setScheduleStartTime(8,0);
        availability.setScheduleEndTime(18, 0);
        availabilityService.create(availability);

        List<Availability> availabilities = availabilityService.getByUser(uid);
        assertEquals(1, availabilities.size());
        assertEquals(8, availabilities.get(0).getScheduleStartTime().getHours());
    }


}
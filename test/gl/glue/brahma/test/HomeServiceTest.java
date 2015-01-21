package gl.glue.brahma.test;

import com.fasterxml.jackson.databind.node.ObjectNode;
import gl.glue.brahma.service.HomeService;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HomeServiceTest extends TransactionalTest {

    private HomeService homeService = new HomeService();

    @Test // Request with invalid user Authentication. User "testClientDummy" not exists
    public void returnSessionsWithInvalidAuthentication() {
        int uid = 90000;
        ObjectNode result = homeService.getSessions(uid);
        assertEquals(null, result);
    }

    @Test // Request sessions with valid user Authentication
    public void returnSessionsOk() {
        int uid = 90000;
        ObjectNode result = homeService.getSessions(uid);
        assertEquals(result.get("programmed").size(), 1);
        assertEquals(result.get("programmed").get(0).get("id").asInt(), 90700);

        assertEquals(result.get("closed").size(), 2);
        assertEquals(result.get("closed").get(0).get("id").asInt(), 90702);
    }
}

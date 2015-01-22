package gl.glue.brahma.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gl.glue.brahma.service.HomeService;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HomeServiceTest extends TransactionalTest {

    private HomeService homeService = new HomeService();

    @Test // Request with invalid user Authentication. User "testClientDummy" not exists
    public void returnSessionsWithInvalidAuthentication() {
        int uid = 99999;
        ObjectNode result = homeService.getSessions(uid);

        JsonNode programmed = result.get("programmed");
        JsonNode underway = result.get("underway");
        JsonNode closed = result.get("closed");

        assertEquals(programmed.size(), 0);
        assertEquals(underway.size(), 0);
        assertEquals(closed.size(), 0);
    }

    @Test // Request sessions with valid user Authentication
    public void returnSessionsOk() {
        int uid = 90000;
        ObjectNode result = homeService.getSessions(uid);

        JsonNode programmed = result.get("programmed");
        assertEquals(programmed.size(), 1);
        assertEquals(programmed.get(0).get("id").asInt(), 90700);

        JsonNode underway = result.get("underway");
        assertEquals(underway.size(), 0);

        JsonNode closed = result.get("closed");
        assertEquals(closed.size(), 2);
        assertEquals(closed.get(0).get("id").asInt(), 90702);
    }
}

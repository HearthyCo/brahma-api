package gl.glue.brahma.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gl.glue.brahma.service.HomeService;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HomeServiceTest extends TransactionalTest {

    private HomeService homeService = new HomeService();

    @Test // Request with invalid user Authentication. User "testClientDummy" not exists
    public void requestSessionsWithInvalidAuthentication() {
        int uid = 99999;
        ObjectNode result = homeService.getSessions(uid);

        JsonNode programmed = result.get("programmed");
        JsonNode underway = result.get("underway");
        JsonNode closed = result.get("closed");

        assertEquals(0, programmed.size());
        assertEquals(0, underway.size());
        assertEquals(0, closed.size());
    }

    @Test // Request sessions with valid user Authentication
    public void requestSessionsOk() {
        int uid = 90000;
        ObjectNode result = homeService.getSessions(uid);

        JsonNode programmed = result.get("programmed");
        assertEquals(2, programmed.size());
        assertEquals(90700, programmed.get(0).get("id").asInt());

        JsonNode underway = result.get("underway");
        assertEquals(0, underway.size());

        JsonNode closed = result.get("closed");
        assertEquals(2, closed.size());
        assertEquals(90702, closed.get(0).get("id").asInt());
    }
}

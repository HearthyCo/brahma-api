package gl.glue.brahma.test.controllers.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import utils.TransactionalTest;
import org.junit.Test;
import play.mvc.Result;
import play.test.FakeRequest;
import utils.TestUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static play.test.Helpers.*;

public class HomeControllerTest extends TransactionalTest {

    @Test // Request without user authentication
    public void testGetHomeUnauthenticated() {
        FakeRequest fr = fakeRequest(GET, "/v1/client/me/home");
        Result result = routeAndCall(fr, REQUEST_TIMEOUT);
        assertNotNull(result);
        assertEquals(401, result.toScala().header().status());
    }

    @Test // Request home
    public void testGetHomeOk() {
        String login = "testClient1@glue.gl";
        Result auth = TestUtils.makeClientLoginRequest(login, login);
        Result result = TestUtils.callController(GET, "/v1/client/me/home", auth);

        assertNotNull(result);
        assertEquals(200, result.toScala().header().status());

        ObjectNode ret = TestUtils.toJson(result);

        // Sessions
        JsonNode programmed = ret.get("sessions").get("programmed");
        JsonNode closed = ret.get("sessions").get("closed");

        assertEquals(2, programmed.size());
        assertEquals(90700, programmed.get(0).get("id").asInt());

        assertEquals(2, closed.size());
        assertEquals(90702, closed.get(0).get("id").asInt());

        // Balance
        assertEquals(20000000, ret.get("balance").get("amount").asInt());
    }
}

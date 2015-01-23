package gl.glue.brahma.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;
import play.mvc.Result;
import play.test.FakeRequest;
import utils.TestUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static play.test.Helpers.*;

public class HomeControllerTest extends TransactionalTest {

    @Test // Request without user authentication
    public void requestHomeWithoutAuthentication() {
        FakeRequest fr = fakeRequest(GET, "/v1/user/home");
        Result result = routeAndCall(fr, REQUEST_TIMEOUT);
        assertNotNull(result);
        assertEquals(401, result.toScala().header().status());
    }

    @Test // Request home
    public void requestHomeOk() {
        String login = "testClient1";
        Result responseLogin = TestUtils.makeLoginRequest(login, login);

        Result result = TestUtils.getHomeRequest(responseLogin);
        assertNotNull(result);
        assertEquals(result.toScala().header().status(), 200);

        ObjectNode ret = TestUtils.toJson(result);

        // Sessions
        JsonNode programmed = ret.get("sessions").get("programmed");
        JsonNode closed = ret.get("sessions").get("closed");

        assertEquals(programmed.size(), 1);
        assertEquals(programmed.get(0).get("id").asInt(), 90700);

        assertEquals(closed.size(), 2);
        assertEquals(closed.get(0).get("id").asInt(), 90702);

        // Balance
        assertEquals(ret.get("balance").get("amount").asInt(), 20000000);
    }
}

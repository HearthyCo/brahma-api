package gl.glue.brahma.test.controllers.professional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;
import play.Logger;
import play.mvc.Result;
import play.test.FakeRequest;
import utils.TestUtils;
import utils.TransactionalTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static play.mvc.Http.Status.REQUEST_TIMEOUT;
import static play.test.Helpers.GET;
import static play.test.Helpers.fakeRequest;
import static play.test.Helpers.routeAndCall;

public class HomeControllerTest extends TransactionalTest {

    @Test // Request without user authentication
    public void testGetHomeUnauthenticated() {
        FakeRequest fr = fakeRequest(GET, "/v1/professional/me/home");
        Result result = routeAndCall(fr, REQUEST_TIMEOUT);
        assertNotNull(result);
        assertEquals(401, result.toScala().header().status());
    }

    @Test // Request home
    public void testGetHomeOk() {
        String login = "testProfessional1@glue.gl";

        Result auth = TestUtils.makeProfessionalLoginRequest(login, login);
        Result result = TestUtils.callController(GET, "/v1/professional/me/home", auth);

        assertNotNull(result);
        assertEquals(200, result.toScala().header().status());

        ObjectNode ret = TestUtils.toJson(result);

        // Sessions
        JsonNode services = ret.get("servicetypes");
        JsonNode sessions = ret.get("sessions");

        assertEquals(3, services.size());
        assertEquals(1, sessions.size());
    }
}

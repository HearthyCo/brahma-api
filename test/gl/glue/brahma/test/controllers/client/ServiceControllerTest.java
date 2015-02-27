package gl.glue.brahma.test.controllers.client;


import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import utils.TransactionalTest;
import org.junit.Test;
import play.mvc.Result;
import play.test.FakeRequest;
import utils.TestUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static play.mvc.Http.Status.REQUEST_TIMEOUT;
import static play.test.Helpers.*;

public class ServiceControllerTest extends TransactionalTest {

    @Test // Request balance without user authentication
    public void testGetServicesUnauthenticated() {
        FakeRequest fr = fakeRequest(GET, "/v1/client/services");
        Result result = routeAndCall(fr, REQUEST_TIMEOUT);
        assertNotNull(result);
        assertEquals(401, result.toScala().header().status());
    }

    @Test // Request balance success
    public void testGetServicesOk() {
        String login = "testClient1@glue.gl";
        Result auth = TestUtils.makeClientLoginRequest(login, login);
        Result result = TestUtils.callController(GET, "/v1/client/services", auth);

        assertNotNull(result);
        assertEquals(200, result.toScala().header().status());

        ObjectNode ret = TestUtils.toJson(result);
        ArrayNode services = (ArrayNode) ret.get("servicetypes");

        assertEquals(3, services.size());

    }
}

package gl.glue.brahma.test;


import com.fasterxml.jackson.databind.node.ObjectNode;
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
    public void requestServicesWithoutAuthentication() {
        FakeRequest fr = fakeRequest(GET, "/v1/services");
        Result result = routeAndCall(fr, REQUEST_TIMEOUT);
        assertNotNull(result);
        assertEquals(401, result.toScala().header().status());
    }

    @Test // Request balance success
    public void requestServicesOk() {
        String login = "testClient1@glue.gl";
        Result responseLogin = TestUtils.makeLoginRequest(login, login);

        Result result = TestUtils.getServicesRequest(responseLogin);

        assertNotNull(result);
        assertEquals(200, result.toScala().header().status());

        ObjectNode ret = TestUtils.toJson(result);
        assertEquals(3, ret.get("services").size());
    }
}

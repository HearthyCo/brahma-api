package gl.glue.brahma.test.controllers.professional;

import com.fasterxml.jackson.databind.node.ObjectNode;
import utils.TransactionalTest;

import play.mvc.Result;
import utils.TestUtils;

import static org.junit.Assert.*;
import static play.test.Helpers.*;

public class SessionControllerTest extends TransactionalTest {

    //@Test // Valid request // TODO DEBUG ERROR
    public void testGetPoolsSize() {
        String login = "testClient1@glue.gl";
        Result auth = TestUtils.makeProfessionalLoginRequest(login, login);

        Result result = TestUtils.callController(GET, "/v1/professional/session/pools", auth);
        ObjectNode ret = TestUtils.toJson(result);

        assertNotNull(result);
        assertEquals(200, result.toScala().header().status());
        assertEquals(1, ret.get("pools").get("90302").asInt());
    }
}
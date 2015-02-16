package gl.glue.brahma.test.controllers.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;
import play.mvc.Result;
import play.test.FakeRequest;
import utils.TestUtils;
import utils.TransactionalTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static play.test.Helpers.*;

public class TransactionControllerTest extends TransactionalTest {

    @Test // Request balance without user authentication
    public void testGetTransactionsUnauthenticated() {
        FakeRequest fr = fakeRequest(GET, "/v1/client/me/balance");
        Result result = routeAndCall(fr, REQUEST_TIMEOUT);
        assertNotNull(result);
        assertEquals(result.toScala().header().status(), 401);
    }

    @Test // Request balance success
    public void testGetTransactionsOk() {
        String login = "testClient1@glue.gl";
        Result auth = TestUtils.makeClientLoginRequest(login, login);

        Result result = TestUtils.callController(GET, "/v1/client/me/balance", auth);

        assertNotNull(result);
        assertEquals(200, result.toScala().header().status());

        ObjectNode ret = TestUtils.toJson(result);

        assertEquals(ret.get("balance").asInt(), 20000000);

        assertEquals(ret.get("transactions").size(), 4);

        int sum = 0;
        for(JsonNode transaction : ret.get("transactions")) {
            sum += transaction.get("amount").asInt();
        }

        assertEquals(ret.get("balance").asInt(), sum);
    }
}

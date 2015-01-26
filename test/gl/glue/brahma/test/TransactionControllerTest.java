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

public class TransactionControllerTest extends TransactionalTest {

    @Test // Request balance without user authentication
    public void requestBalanceWithoutAuthentication() {
        FakeRequest fr = fakeRequest(GET, "/v1/user/balance");
        Result result = routeAndCall(fr, REQUEST_TIMEOUT);
        assertNotNull(result);
        assertEquals(result.toScala().header().status(), 401);
    }

    @Test // Request balance success
    public void requestBalanceOk() {
        String login = "testClient1";
        Result responseLogin = TestUtils.makeLoginRequest(login, login);

        Result result = TestUtils.getTransactionRequest(responseLogin);

        assertNotNull(result);
        assertEquals(result.toScala().header().status(), 200);

        ObjectNode ret = TestUtils.toJson(result);

        JsonNode balance = ret.get("balance");
        assertEquals(balance.get("amount").asInt(), 20000000);

        assertEquals(balance.get("transactions").size(), 4);

        int sum = 0;
        for(JsonNode transaction : balance.get("transactions")) {
            sum += transaction.get("amount").asInt();
        }

        assertEquals(ret.get("balance").get("amount").asInt(), sum);
    }
}

package gl.glue.brahma.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gl.glue.brahma.service.BalanceService;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TransactionServiceTest extends TransactionalTest  {

    private BalanceService balanceService = new BalanceService();

    @Test // Request sessions with valid user Authentication
    public void requestBalanceOk() {
        int uid = 90000;
        ObjectNode result = balanceService.getBalance(uid);

        assertNotNull(result);
        assertEquals(result.get("amount").asInt(), 20000000);
        assertEquals(result.get("transactions").size(), 4);

        int sum = 0;
        for(JsonNode transaction : result.get("transactions")) {
            sum += transaction.get("amount").asInt();
        }

        assertEquals(result.get("amount").asInt(), sum);
    }

    @Test // Request sessions with valid user Authentication
    public void requestAmountOk() {
        int uid = 90000;
        int result = balanceService.getAmount(uid);
        assertEquals(result, 20000000);
    }
}

package gl.glue.brahma.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gl.glue.brahma.model.transaction.Transaction;
import gl.glue.brahma.model.user.User;
import gl.glue.brahma.service.TransactionService;
import gl.glue.brahma.service.UserService;
import org.junit.Test;
import utils.FakePaypalHelper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TransactionServiceTest extends TransactionalTest  {

    private TransactionService transactionService = new TransactionService();
    private UserService userService = new UserService();

    @Test // Request sessions with valid user Authentication
    public void requestBalanceOk() {
        int uid = 90000;
        ObjectNode result = transactionService.getUserTransactions(uid);

        assertNotNull(result);
        assertEquals(result.get("amount").asInt(), 20000000);
        assertEquals(result.get("transactions").size(), 4);

        int sum = 0;
        for(JsonNode transaction : result.get("transactions")) {
            sum += transaction.get("amount").asInt();
        }

        assertEquals(result.get("amount").asInt(), sum);
    }

    @Test
    public void completePaypalTransaction() {
        int uid = 90000;
        int amount = 5000;
        String baseUrl = "http://localhost:9000";
        String approvalUrl = "http://example.com/pay";

        User user = userService.getById(uid);
        int beforeBalance = user.getBalance();

        transactionService.setPaypalHelper(new FakePaypalHelper());
        Transaction transaction = transactionService.createPaypalTransaction(uid, amount, baseUrl);

        assertEquals(uid, transaction.getUser().getId());
        assertEquals(amount, transaction.getAmount());
        assertEquals(Transaction.State.INPROGRESS, transaction.getState());
        assertEquals(approvalUrl, transaction.getMeta().get("paypal").get("links").get(1).get("href").asText());

        Transaction transaction2;
        transaction2 = transactionService.executePaypalTransaction("some-token", transaction.getSku(), "some-payerid");
        int afterBalance = user.getBalance();

        assertEquals(Transaction.State.APPROVED, transaction2.getState());
        assertEquals(beforeBalance + amount, afterBalance);

    }

}

package gl.glue.brahma.test.service;

import gl.glue.brahma.model.transaction.Transaction;
import gl.glue.brahma.model.user.User;
import gl.glue.brahma.service.TransactionService;
import gl.glue.brahma.service.UserService;
import org.junit.Test;
import utils.FakePaypalHelper;
import utils.TransactionalTest;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TransactionServiceTest extends TransactionalTest {

    private TransactionService transactionService = new TransactionService();
    private UserService userService = new UserService();

    @Test // Request sessions with valid user Authentication
    public void requestBalanceOk() {
        int uid = 90000;
        List<Transaction> result = transactionService.getUserTransactions(uid);

        assertNotNull(result);
        assertEquals(result.size(), 4);
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

package gl.glue.brahma.test.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import gl.glue.brahma.model.transaction.Transaction;
import gl.glue.brahma.model.user.User;
import gl.glue.brahma.service.TransactionService;
import gl.glue.brahma.service.UserService;
import org.junit.BeforeClass;
import org.junit.Test;
import play.libs.Json;
import utils.FakePaypalHelper;
import utils.TransactionalTest;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TransactionServiceTest extends TransactionalTest {

    private static TransactionService transactionService = new TransactionService();
    private UserService userService = new UserService();

    @BeforeClass
    public static void setup() {
        transactionService.setPaypalHelper(new FakePaypalHelper());
    }

    @Test
    public void getTransactionOk() {
        Transaction transaction = transactionService.getTransaction(91301);
        assertEquals(-1000, transaction.getAmount());
    }

    @Test // Request sessions with valid user Authentication
    public void requestBalanceOk() {
        int uid = 90000;
        List<Transaction> result = transactionService.getUserTransactions(uid);

        assertNotNull(result);
        assertEquals(result.size(), 4);
    }

    @Test
    public void createAndexecutePaypalTransactionOk() {
        int uid = 90000;
        int amount = 5000;
        ObjectNode rUrls = Json.newObject();
        rUrls.put("success", "http://localhost:9000/success");
        rUrls.put("cancel", "http://localhost:9000/cancel");

        String approvalUrl = "http://example.com/pay";

        User user = userService.getById(uid);
        int beforeBalance = user.getBalance();

        Transaction transaction = transactionService.createPaypalTransaction(uid, amount, rUrls);

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

    @Test
    public void capturePaypalTransactionOk() {
        int uid = 90000;
        int amount = 5000;

        Transaction transaction = transactionService.capturePaypalTransaction(uid, "some-authentication-id", amount);

        assertEquals(uid, transaction.getUser().getId());
        assertEquals(amount, transaction.getAmount());
        assertEquals(Transaction.State.APPROVED, transaction.getState());
    }

}

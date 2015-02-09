package gl.glue.brahma.service;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gl.glue.brahma.model.session.Session;
import gl.glue.brahma.model.transaction.Transaction;
import gl.glue.brahma.model.transaction.TransactionDao;
import gl.glue.brahma.model.user.User;
import gl.glue.brahma.model.user.UserDao;
import gl.glue.brahma.util.PaypalHelper;
import play.db.jpa.Transactional;
import play.libs.Json;

import java.util.List;

public class TransactionService {

    private UserDao userDao = new UserDao();
    private TransactionDao transactionDao = new TransactionDao();
    private PaypalHelper paypalHelper = new PaypalHelper();

    @Transactional
    public Transaction getTransaction(int id) {
        Transaction transaction = transactionDao.getById(id);

        return transaction;
    }

    public void setPaypalHelper(PaypalHelper paypalHelper) {
        this.paypalHelper = paypalHelper;
    }

    /**
     * Search in transactions with uid
     * @param uid User id
     * @return {ObjectNode} Balance of user with a current amount and a transaction list
     */
    public ObjectNode getUserTransactions(int uid, int limit) {
        User user = userDao.findById(uid);
        List<Transaction> transactionList = transactionDao.getTransactionHistory(uid, limit);
        ArrayNode transactions = new ArrayNode(JsonNodeFactory.instance);

        if(!transactionList.isEmpty()) {
            for(Transaction transaction : transactionList) {
                ObjectNode transactionObject = (ObjectNode) Json.toJson(transaction);
                Session s = transaction.getSession();
                if (s != null) {
                    transactionObject.put("title", s.getTitle());
                }
                transactions.add(transactionObject);
            }
        }

        ObjectNode result = Json.newObject();
        result.put("amount", user.getBalance());
        result.put("transactions", transactions);

        return result;
    }

    public ObjectNode getUserTransactions(int uid) {
        return getUserTransactions(uid, 0);
    }

    /**
     *
     * @param uid
     * @param amount
     * @return
     */
    @Transactional
    public Transaction createPaypalTransaction(int uid, int amount, String baseUrl) {
        User user = userDao.findById(uid);
        if (user == null) return null;

        PaypalHelper.PaypalPayment payment = paypalHelper.createPaypalTransaction(amount, baseUrl);
        Transaction transaction = new Transaction(user, amount, payment.getState(), payment.getSku(), payment.getTitle());

        ObjectNode meta = Json.newObject();
        meta.put("paypal", payment.getMeta());
        transaction.setMeta(meta);

        transactionDao.create(transaction);
        return transaction;
    }

    /**
     *
     * @param token
     * @param paypalId
     * @param payerId
     * @return
     */
    @Transactional
    public Transaction executePaypalTransaction(String token, String paypalId, String payerId) {
        Transaction transaction = transactionDao.getBySku(paypalId);
        if(transaction.getState() != Transaction.State.INPROGRESS) return null;

        PaypalHelper.PaypalPayment payment = paypalHelper.executePaypalTransaction(paypalId, payerId);
        if (payment == null) return null;

        transaction.setState(payment.getState());

        ObjectNode meta = (ObjectNode) transaction.getMeta();
        meta.put("paypal", payment.getMeta());
        transaction.setMeta(meta);

        User user = transaction.getUser();
        user.setBalance(transactionDao.getUserBalance(user.getId()));

        return transaction;
    }
}

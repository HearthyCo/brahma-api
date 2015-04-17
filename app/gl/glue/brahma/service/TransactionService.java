package gl.glue.brahma.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
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

    /**
     * Service for find in transaction in database
     * @param id Transaction identificator for search
     * @return Transaction with id passed
     */
    @Transactional
    public Transaction getTransaction(int id) {
        return transactionDao.getById(id);
    }

    public void setPaypalHelper(PaypalHelper paypalHelper) {
        this.paypalHelper = paypalHelper;
    }

    /**
     * Search in transactions with uid
     * @param uid User id
     * @return {ObjectNode} Balance of user with a current amount and a transaction list
     */
    public List<Transaction> getUserTransactions(int uid, int limit) {
        return transactionDao.getTransactionHistory(uid, limit);
    }

    public List<Transaction> getUserTransactions(int uid) {
        return getUserTransactions(uid, 0);
    }

    /**
     * Service for create a paypal transaction, calls paypalHelper to return a paypal payment and append
     * in transaction meta
     * @param uid User identificator which create transaction
     * @param amount Amount in cents for create transaction
     * @return A valid transaction in inprogress status
     */
    @Transactional
    public Transaction createPaypalTransaction(int uid, int amount, ObjectNode redirectUrls) {
        User user = userDao.findById(uid);
        if (user == null) return null;

        PaypalHelper.PaypalPayment payment = paypalHelper.createPaypalTransaction(amount, redirectUrls);
        Transaction transaction = new Transaction(
                user, amount, payment.getState(), payment.getSku(), Transaction.Reason.TOP_UP);

        ObjectNode meta = Json.newObject();
        meta.put("paypal", payment.getMeta());
        transaction.setMeta(meta);

        transactionDao.create(transaction);
        return transaction;
    }

    /**
     * Service for execute paypal transaction previously created
     * @param token ?
     * @param paypalId Paypal identificator
     * @param payerId User id which paid
     * @return  A valid transaction in approved status
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

    /**
     * Service for capturing paypal transactions previously authorized by the mobile sdk
     * @param authorizationId Paypal authorization identificator
     * @param amount Amount of credits to capture
     * @return A valid transaction in approved status
     */
    @Transactional
    public Transaction capturePaypalTransaction(int uid, String authorizationId, int amount) {
        User user = userDao.findById(uid);
        if (user == null) return null;

        PaypalHelper.PaypalPayment payment = paypalHelper.capturePaypalTransaction(authorizationId, amount);
        if (payment == null) return null;

        Transaction transaction = new Transaction(
                user, amount, payment.getState(), payment.getSku(), Transaction.Reason.TOP_UP);
        transactionDao.create(transaction);

        ObjectNode meta = (ObjectNode) transaction.getMeta();
        meta.put("paypal", payment.getMeta());
        transaction.setMeta(meta);

        user.setBalance(transactionDao.getUserBalance(user.getId()));

        return transaction;
    }
}

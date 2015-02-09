package gl.glue.brahma.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import gl.glue.brahma.model.transaction.Transaction;
import gl.glue.brahma.model.transaction.TransactionDao;
import gl.glue.brahma.model.user.User;
import gl.glue.brahma.model.user.UserDao;
import gl.glue.brahma.util.PaypalHelper;
import play.db.jpa.Transactional;
import play.libs.Json;

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
    public Transaction executePay(String token, String paypalId, String payerId) {
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

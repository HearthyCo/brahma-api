package gl.glue.brahma.service;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.paypal.api.payments.Amount;
import com.paypal.api.payments.Payer;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.RedirectUrls;
import com.paypal.core.rest.APIContext;
import com.paypal.core.rest.OAuthTokenCredential;
import com.paypal.core.rest.PayPalRESTException;
import gl.glue.brahma.model.session.Session;
import gl.glue.brahma.model.transaction.Transaction;
import gl.glue.brahma.model.transaction.TransactionDao;
import gl.glue.brahma.model.user.User;
import gl.glue.brahma.model.user.UserDao;
import play.libs.Json;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TransactionService {

    private final static String CURRENCY = "EUR";
    private final static String CLIENT_ID = "AR-PoBBvPqAFyx1uFFkXa9Xd07XSOacB8wRbRFE25GtC7iRyRwaNq-Mqp0JU";
    private final static String SECRET = "EIYC6xAfIANsvMN59HxgmSpK2O9A9b9-Liv99YVC1sM0ATflklILlUtwJvbg";

    private UserDao userDao = new UserDao();
    private TransactionDao transactionDao = new TransactionDao();

    /**
     * Search in transactions with uid
     * @param uid User id
     * @return {ObjectNode} Balance of user with a current amount and a transaction list
     */
    public ObjectNode getBalance(int uid) {
        List<Transaction> transactionList = transactionDao.getTransactionHistory(uid);
        ArrayNode transactions = new ArrayNode(JsonNodeFactory.instance);

        if(!transactionList.isEmpty()) {
            for(Transaction transaction : transactionList) {
                ObjectNode transactionObject = (ObjectNode) Json.toJson(transaction);
                transactionObject.put("title", transaction.getSession().getTitle());

                transactions.add(transactionObject);
            }
        }

        ObjectNode result = Json.newObject();
        result.put("amount", getAmount(uid));
        result.put("transactions", transactions);

        return result;
    }

    /**
     * Find user by uid for get current amount
     * @param uid User id
     * @return {int} Current amount
     */
    public int getAmount(int uid) {
        User user = userDao.findById(uid);
        return user.getBalance();
    }

    private ObjectNode getUserToken() {
        try {
            ObjectNode token = (ObjectNode) Json.parse(new OAuthTokenCredential(CLIENT_ID, SECRET).getAccessToken());
            Date now = new Date();
            now.setTime(now.getTime() + token.get("expired_in").asLong());
            token.put("expires_on", now.getTime());

            return (ObjectNode) Json.parse(new OAuthTokenCredential(CLIENT_ID, SECRET).getAccessToken());
        } catch (PayPalRESTException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Transaction createTransaction(int uid, int amount, Session session) {

        User user = userDao.findById(uid);

        ObjectNode userMeta = (ObjectNode) user.getMeta();
        ObjectNode token = userMeta.has("token") ? (ObjectNode) userMeta.get("token") : null;

        Date now = new Date();
        if(token == null || (token.has("expires_on") && now.after(new Date(token.get("expires_on").asLong())))) {
            token = getUserToken();

            if(token != null) {
                token.put("contextToken", token.get("accessToken") + " " + token.get("token_type"));
                ObjectNode meta = Json.newObject();
                meta.put("token", token);

                user.setMeta(meta);
                userDao.save(user);
            }
        }

        if(!token.has("contextToken")) return null;

        APIContext apiContext = new APIContext(token.get("contextToken").asText());

        Amount amountPay = new Amount();
        amountPay.setCurrency(CURRENCY);
        amountPay.setTotal("12");

        String reason = (session == null) ? "Add credits to your account" : "Payment of the session " + session.getTitle();

        com.paypal.api.payments.Transaction transaction = new com.paypal.api.payments.Transaction();
        transaction.setDescription(reason);
        transaction.setAmount(amountPay);

        List<com.paypal.api.payments.Transaction> transactions = new ArrayList<>();
        transactions.add(transaction);

        Payer payer = new Payer();
        payer.setPaymentMethod("paypal");

        Payment payment = new Payment();
        payment.setIntent("sale");
        payment.setPayer(payer);
        payment.setTransactions(transactions);

        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl("/v1/transaction/url/cancel");
        redirectUrls.setReturnUrl("/v1/transaction/url/success");
        payment.setRedirectUrls(redirectUrls);

        try {
            Payment createdPayment = payment.create(apiContext);

            ObjectNode paypalPayment = Json.newObject();

            paypalPayment.put("paypal", Json.toJson(createdPayment));

            Transaction paypalTransaction = new Transaction();

            paypalTransaction.setUser(user);
            paypalTransaction.setSession(session);
            paypalTransaction.setAmount(amount);
            paypalTransaction.setState(Transaction.State.valueOf(createdPayment.getState()));
            paypalTransaction.setTimestamp(new Date());
            paypalTransaction.setReason(reason);

            paypalTransaction.setMeta(paypalPayment);

            return paypalTransaction;

        } catch (PayPalRESTException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Transaction createTransaction(int uid, int amount) {
        return createTransaction(uid, amount, null);
    }

    /*
    public Transaction executeTransaction(Transaction paypalTransaction) {
        Confirm transaction change transaction state and update user amount
    }
    */
}

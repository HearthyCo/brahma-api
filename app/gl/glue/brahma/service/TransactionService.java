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
import gl.glue.brahma.model.transaction.Paypal;
import gl.glue.brahma.model.transaction.Transaction;
import gl.glue.brahma.model.transaction.TransactionDao;
import gl.glue.brahma.model.user.User;
import gl.glue.brahma.model.user.UserDao;
import play.libs.Json;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TransactionService {

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

    public void newTransaction(int uid) {

        User user = userDao.findById(uid);
        ObjectNode token = null;

        if(user.getToken().has("expires_on")) {
            Date expires_on = new Date(user.getToken().get("expires_on").asLong());
            Date now = new Date();

            if (now.before(expires_on)) {
                token = (ObjectNode) user.getToken();
            }
            else {
                token = getUserToken();
                user.setToken(token);
                userDao.save(user);
            }
        }

        if(token != null) {
            user.setToken(token);

            if(token.has("accessToken") && token.has("token_type")) {

                String accessToken = token.has("accessToken") + " " + token.has("token_type");

                APIContext apiContext = new APIContext(accessToken);

                Amount amount = new Amount();
                amount.setCurrency("USD");
                amount.setTotal("12");

                com.paypal.api.payments.Transaction transaction = new com.paypal.api.payments.Transaction();
                transaction.setDescription("creating a payment");
                transaction.setAmount(amount);

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

                    Paypal paypalTransaction = new Paypal();
                    // initialize here paypalTransaction


                } catch (PayPalRESTException e) {
                    e.printStackTrace();
                }
            }
        }



    }
}

package gl.glue.brahma.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.OAuthTokenCredential;
import com.paypal.base.rest.PayPalRESTException;
import gl.glue.brahma.model.transaction.Transaction;
import gl.glue.brahma.model.transaction.TransactionDao;
import gl.glue.brahma.model.user.User;
import gl.glue.brahma.model.user.UserDao;
import play.Logger;
import play.db.jpa.Transactional;
import play.libs.Json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransactionService {

    // private static enum TYPES { TOPUP }
    // private static enum TPV { PPL }
    private final static String REASON = "Topup your account";
    private final static String CURRENCY = "EUR";
    private final static String CLIENT_ID = "AR-PoBBvPqAFyx1uFFkXa9Xd07XSOacB8wRbRFE25GtC7iRyRwaNq-Mqp0JU";
    private final static String SECRET = "EIYC6xAfIANsvMN59HxgmSpK2O9A9b9-Liv99YVC1sM0ATflklILlUtwJvbg";
    private static final Map<String, String> sdkConfig;
    private static final Map<String, Transaction.State> paypalStates = new HashMap<>();
    private static String contextToken = null;

    static
    {
        // Initialize paypal sdk configuration
        sdkConfig = new HashMap<>();
        sdkConfig.put("mode", "sandbox");

        // Initialize brahma dictionary states
        paypalStates.put("created", Transaction.State.INPROGRESS);
        paypalStates.put("approved", Transaction.State.APPROVED);
        paypalStates.put("failed", Transaction.State.FAILED);
        paypalStates.put("cancelled", Transaction.State.FAILED);
        paypalStates.put("expired", Transaction.State.FAILED);
        paypalStates.put("pending", Transaction.State.INPROGRESS);

        contextToken = getToken();
    }

    private UserDao userDao = new UserDao();
    private TransactionDao transactionDao = new TransactionDao();


    @Transactional
    public Transaction getTransaction(int id) {
        Transaction transaction = transactionDao.getById(id);

        return transaction;
    }

    private static String getToken() {
        try {
            String token = new OAuthTokenCredential(CLIENT_ID, SECRET, sdkConfig).getAccessToken();
            Logger.info("TOKEN " + token);
            return token;
        } catch (PayPalRESTException e) {
            e.printStackTrace();
        }

        return null;
    }

    /*private String generatePaypalSKU(TYPES type, TPV tpv, int uid) {

        Date now = new Date();
        String userId = String.format("%012d", uid);
        String timestamp = String.format("%016d", now.getTime());

        return StringUtils.join(new String[]{ type.name() + tpv.name(), userId, timestamp }, "_");
    }*/

    /**
     *
     * @param uid
     * @param amount
     * @return
     */
    @Transactional
    public Transaction createPaypalTransaction(int uid, int amount) {
        User user = userDao.findById(uid);

        if(contextToken == null || user == null || amount <= 0) return null;

        APIContext apiContext = new APIContext(contextToken);
        apiContext.setConfigurationMap(sdkConfig);

        String formattedAmount = String.valueOf(amount / 100);

        // Create and add item in list item
        List<Item> listItems = new ArrayList<>();
        listItems.add(new Item("1", "Credits", formattedAmount, CURRENCY));

        com.paypal.api.payments.Transaction paypalTransaction = new com.paypal.api.payments.Transaction();
        paypalTransaction.setDescription(REASON);
        paypalTransaction.setAmount(new Amount(CURRENCY, formattedAmount));
        paypalTransaction.setItemList(new ItemList().setItems(listItems));

        List<com.paypal.api.payments.Transaction> paypalTransactions = new ArrayList<>();
        paypalTransactions.add(paypalTransaction);

        Payer payer = new Payer("paypal");

        Payment payment = new Payment("sale", payer);
        payment.setTransactions(paypalTransactions);

        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl("http://localhost:3000/#transaction/url/cancel");
        redirectUrls.setReturnUrl("http://localhost:3000/#transaction/url/success");

        payment.setRedirectUrls(redirectUrls);

        Transaction transaction;

        try {
            payment = payment.create(apiContext);

            transaction = new Transaction(user, amount, paypalStates.get(payment.getState()), payment.getId(), REASON);

            ObjectNode meta = Json.newObject();
            meta.put("paypal", Json.toJson(payment));
            transaction.setMeta(meta);

            transactionDao.create(transaction);

        } catch (PayPalRESTException e) {
            e.printStackTrace();
            return null;
        }

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
        // Confirm transaction change transaction state and update user amount
        APIContext apiContext = new APIContext(contextToken);
        apiContext.setConfigurationMap(sdkConfig);

        Transaction transaction = transactionDao.getBySku(paypalId);

        if(transaction.getState() != Transaction.State.INPROGRESS) return null;

        Payment payment = new Payment();
        payment.setId(paypalId);

        try {
            PaymentExecution paymentExecute = new PaymentExecution(payerId);

            payment = payment.execute(apiContext, paymentExecute);

            transaction.setState(paypalStates.get(payment.getState()));

            ObjectNode meta = (ObjectNode) transaction.getMeta();
            meta.put("paypal", Json.toJson(payment));
            transaction.setMeta(meta);

            User user = transaction.getUser();
            user.setBalance(transactionDao.getUserBalance(user.getId()));

            return transaction;

        } catch (PayPalRESTException e) {
            e.printStackTrace();
        }

        return null;
    }
}

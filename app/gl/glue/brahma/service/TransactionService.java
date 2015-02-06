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
import org.apache.commons.lang3.StringUtils;
import play.Logger;
import play.libs.Json;

import java.util.*;

public class TransactionService {

    private static enum TYPES { TOPUP }
    private final static String PAYPAL = "PPL";
    private final static String CURRENCY = "EUR";
    private final static String CLIENT_ID = "AR-PoBBvPqAFyx1uFFkXa9Xd07XSOacB8wRbRFE25GtC7iRyRwaNq-Mqp0JU";
    private final static String SECRET = "EIYC6xAfIANsvMN59HxgmSpK2O9A9b9-Liv99YVC1sM0ATflklILlUtwJvbg";

    private UserDao userDao = new UserDao();
    private TransactionDao transactionDao = new TransactionDao();


    public Transaction getTransaction(int uid, int id) {

        Transaction transaction = transactionDao.get(id);

        if(transaction.getState() == Transaction.State.INPROGRESS ||
                transaction.getState() == Transaction.State.PENDING) {

            if (transaction.getMeta().has("paypal")) {
                ObjectNode paypal = (ObjectNode) transaction.getMeta().get("paypal");

                String contextToken = getToken();
                String paypalId = paypal.get("id").asText();

                Payment payment = new Payment();
                try {
                    payment.get(contextToken, paypalId);
                    transaction = updatePaypalTransaction(transaction, payment);
                } catch (PayPalRESTException e) {
                    e.printStackTrace();
                }

            }
        }

        return transaction;
    }

    private  Transaction updatePaypalTransaction(Transaction transaction, Payment payment) {
        Map<String, Transaction.State> paypalStates = new HashMap<>();

        paypalStates.put("created", Transaction.State.INPROGRESS);
        paypalStates.put("approved", Transaction.State.APPROVED);
        paypalStates.put("failed", Transaction.State.FAILED);
        paypalStates.put("cancelled", Transaction.State.FAILED);
        paypalStates.put("expired", Transaction.State.FAILED);
        paypalStates.put("pending", Transaction.State.PENDING);

        transaction.setState(paypalStates.get(payment.getState()));

        ObjectNode meta = (ObjectNode) transaction.getMeta();
        meta.put("paypal", Json.toJson(payment));

        transaction.setMeta(meta);

        return transaction;
    }

    private String getToken() {
        Map<String, String> sdkConfig = new HashMap<>();
        sdkConfig.put("mode", "sandbox");

        try {
            String token = new OAuthTokenCredential(CLIENT_ID, SECRET, sdkConfig).getAccessToken();
            Logger.info("TOKEN " + token);
            return token;
        } catch (PayPalRESTException e) {
            e.printStackTrace();
        }

        return null;
    }

    private String generatePaypalSKU(int uid) {

        Date now = new Date();
        String userId = String.format("%012d", uid);
        Logger.info("USER ID " + userId);
        String timestamp = String.format("%016d", now.getTime());
        Logger.info("TIMESTAMP " + timestamp);

        return StringUtils.join(new String[]{ TYPES.TOPUP + PAYPAL, userId, timestamp }, "_");
    }

    public Transaction createPaypalTransaction(int uid, int amount) {

        Map<String, String> sdkConfig = new HashMap<>();
        sdkConfig.put("mode", "sandbox");

        User user = userDao.findById(uid);
        String contextToken = getToken();

        if(contextToken == null) return null;

        APIContext apiContext = new APIContext(contextToken);
        apiContext.setConfigurationMap(sdkConfig);

        String paypalSKU = generatePaypalSKU(uid);
        Logger.info("SKU " + paypalSKU);
        String realAmount = String.valueOf(amount / 100);
        String reason = "Topup your account";

        // Create item
        Item item = new Item();
        item.setName("Credits");
        item.setPrice(realAmount);
        item.setCurrency(CURRENCY);
        item.setQuantity("1");

        // Create and add item in list item
        List<Item> listItems = new ArrayList<>();
        listItems.add(item);

        //  Create ItemList and set list of items
        ItemList itemList = new ItemList();
        itemList.setItems(listItems);

        Amount amountPay = new Amount();
        amountPay.setCurrency(CURRENCY);
        amountPay.setTotal(realAmount);

        com.paypal.api.payments.Transaction transaction = new com.paypal.api.payments.Transaction();
        transaction.setDescription(reason);
        transaction.setAmount(amountPay);
        transaction.setItemList(itemList);
        transaction.setItemList(itemList);
        transaction.setInvoiceNumber(paypalSKU);

        List<com.paypal.api.payments.Transaction> transactions = new ArrayList<>();
        transactions.add(transaction);

        Payer payer = new Payer();
        payer.setPaymentMethod("paypal");

        Payment payment = new Payment();
        payment.setIntent("sale");
        payment.setPayer(payer);
        payment.setTransactions(transactions);

        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl("http://localhost:3000/#transaction/url/cancel");
        redirectUrls.setReturnUrl("http://localhost:3000/#transaction/url/success");

        payment.setRedirectUrls(redirectUrls);

        Transaction paypalTransaction;

        try {
            Payment createdPayment = payment.create(apiContext);

            paypalTransaction = new Transaction();

            paypalTransaction.setUser(user);
            paypalTransaction.setAmount(amount);
            paypalTransaction.setTimestamp(new Date());
            paypalTransaction.setReason(reason);
            paypalTransaction.setSku(paypalSKU);

            paypalTransaction = updatePaypalTransaction(paypalTransaction, createdPayment);
        } catch (PayPalRESTException e) {
            e.printStackTrace();
            return null;
        }

        return paypalTransaction;
    }

    /*
    public Transaction executeTransaction(Transaction paypalTransaction) {
        Confirm transaction change transaction state and update user amount
    }
    */
}

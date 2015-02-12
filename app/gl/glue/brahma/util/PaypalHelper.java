package gl.glue.brahma.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.OAuthTokenCredential;
import com.paypal.base.rest.PayPalRESTException;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import gl.glue.brahma.model.transaction.Transaction;

import play.libs.Json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PaypalHelper {

    private static Config conf = null;
    private static Map<String, String> sdkConfig;
    private static Map<String, Transaction.State> paypalStateTranslator = new HashMap<>();

    private static OAuthTokenCredential credential;
    private static String contextToken;

    /**
     * Subclass PaypalPayment
     * Used to define a paypal payment
     */
    public class PaypalPayment {
        private String sku;
        private String title;
        private Transaction.State state;
        private JsonNode meta;

        public String getSku() {
            return sku;
        }

        public String getTitle() {
            return title;
        }

        public Transaction.State getState() {
            return state;
        }

        public JsonNode getMeta() {
            return meta;
        }

        public PaypalPayment(String sku, String title, Transaction.State state, JsonNode meta) {
            this.sku = sku;
            this.title = title;
            this.state = state;
            this.meta = meta;
        }
    }

    /**
     * Initialization static methods
     */
    static {
        conf = ConfigFactory.load();

        // Initialize paypal sdk configuration
        sdkConfig = new HashMap<>();
        sdkConfig.put("mode", "sandbox");

        // Initialize brahma dictionary states
        paypalStateTranslator.put("created", Transaction.State.INPROGRESS);
        paypalStateTranslator.put("approved", Transaction.State.APPROVED);
        paypalStateTranslator.put("failed", Transaction.State.FAILED);
        paypalStateTranslator.put("cancelled", Transaction.State.FAILED);
        paypalStateTranslator.put("expired", Transaction.State.FAILED);
        paypalStateTranslator.put("pending", Transaction.State.INPROGRESS);
    }

    /**
     * Class Contructor
     */
    public PaypalHelper() {
        checkToken();
    }

    /**
     * Get token
     * @return Return a valid token from clientId and secret
     */
    protected void checkToken() {
        // Is the token still valid? No need to mess with concurrency then.
        if (credential != null && credential.expiresIn() > 300) {
            return;
        }
        // We have to update it. Only one thread should do this, so we have to lock on a
        // shared object across all instances, instead of <this>.
        synchronized (sdkConfig) {
            // Some other thread might have updated it while we were waiting. Check it again.
            if (credential != null && credential.expiresIn() > 300) {
                return;
            }
            // Ok, so we have to update it after all.
            try {
                OAuthTokenCredential newCredential = new OAuthTokenCredential(
                        conf.getString("paypal.clientId"),
                        conf.getString("paypal.secret"),
                        sdkConfig
                );
                contextToken = newCredential.getAccessToken();
                credential = newCredential;
            } catch (PayPalRESTException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This function creates a valid paypal payment
     * @param amount Amount in cents for create transaction
     * @param baseUrl Url base where paypal have to redirect user after login
     * @return PaypalPayment in in progress status
     */
    public PaypalPayment createPaypalTransaction(int amount, String baseUrl) {
        checkToken();
        if (contextToken == null || amount <= 0) return null;

        APIContext apiContext = new APIContext(contextToken);
        apiContext.setConfigurationMap(sdkConfig);

        String formattedAmount = String.valueOf(amount / 100);

        // Create and add item in list item
        List<Item> listItems = new ArrayList<>();
        listItems.add(new Item("1", "Credits", formattedAmount, conf.getString("paypal.currency")));

        com.paypal.api.payments.Transaction paypalTransaction = new com.paypal.api.payments.Transaction();
        paypalTransaction.setDescription(conf.getString("paypal.reason"));
        paypalTransaction.setAmount(new Amount(conf.getString("paypal.currency"), formattedAmount));
        paypalTransaction.setItemList(new ItemList().setItems(listItems));

        List<com.paypal.api.payments.Transaction> paypalTransactions = new ArrayList<>();
        paypalTransactions.add(paypalTransaction);

        Payer payer = new Payer("paypal");

        Payment payment = new Payment("sale", payer);
        payment.setTransactions(paypalTransactions);

        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl(baseUrl + "/cancel");
        redirectUrls.setReturnUrl(baseUrl + "/success");

        payment.setRedirectUrls(redirectUrls);

        try {
            payment = payment.create(apiContext);
            return new PaypalPayment(
                    payment.getId(),
                    conf.getString("paypal.reason"),
                    paypalStateTranslator.get(payment.getState()),
                    Json.toJson(payment));
        } catch (PayPalRESTException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * This function execute a transacion previously created
     * @param sku Paypal transaction identificator
     * @param payerId User id which paid
     * @return PaypalPayment in approved status
     */
    public PaypalPayment executePaypalTransaction(String sku, String payerId) {
        checkToken();
        APIContext apiContext = new APIContext(contextToken);
        apiContext.setConfigurationMap(sdkConfig);

        Payment payment = new Payment();
        payment.setId(sku);

        try {
            PaymentExecution paymentExecute = new PaymentExecution(payerId);
            payment = payment.execute(apiContext, paymentExecute);

            return new PaypalPayment(
                    payment.getId(),
                    conf.getString("paypal.reason"),
                    paypalStateTranslator.get(payment.getState()),
                    Json.toJson(payment));

        } catch (PayPalRESTException e) {
            e.printStackTrace();
        }

        return null;

    }

}

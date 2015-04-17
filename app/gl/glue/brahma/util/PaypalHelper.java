package gl.glue.brahma.util;

import com.fasterxml.jackson.databind.node.ObjectNode;

public interface PaypalHelper {

    /**
     * Create a new Paypal payment.
     * @param amount Amount of the transaction in cents
     * @param rUrls Callback URLs. Should be an object containing two keys: success and cancel.
     * @return A new PaypalPayment in progress state
     */
    PaypalPayment createPaypalTransaction(int amount, ObjectNode rUrls);

    /**
     * Execute a previously created transaction.
     * @param sku Paypal transaction identifier
     * @param payerId User id which paid the transaction
     * @return A PaypalPayment in approved status
     */
    PaypalPayment executePaypalTransaction(String sku, String payerId);

    /**
     * Capture a payment previously authorized by the mobile sdk.
     * @param authorizationId The id of the authorization
     * @return A PaypalPayment in completed status
     */
    PaypalPayment capturePaypalTransaction(String authorizationId, int amount);
}

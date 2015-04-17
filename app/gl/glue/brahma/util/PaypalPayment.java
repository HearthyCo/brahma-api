package gl.glue.brahma.util;

import com.fasterxml.jackson.databind.JsonNode;
import gl.glue.brahma.model.transaction.Transaction;

/**
 * DTO to keep a Paypal payment metadata
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

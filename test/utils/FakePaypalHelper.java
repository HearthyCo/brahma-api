package utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gl.glue.brahma.model.transaction.Transaction;
import gl.glue.brahma.util.PaypalHelper;
import play.libs.Json;

public class FakePaypalHelper extends PaypalHelper {

    @Override
    protected String getToken() {
        return "~~~TOKEN~~~";
    }

    @Override
    public PaypalPayment createPaypalTransaction(int amount, String baseUrl) {
        ObjectNode meta = Json.newObject();
        ObjectNode link = Json.newObject();
        link.put("href", "http://example.com/pay");
        ArrayNode links = new ArrayNode(JsonNodeFactory.instance);
        links.add(link);
        links.add(link);
        meta.put("links", links);
        return new PaypalPayment("PAY-TEST-SKU", "TOPUP TEST", Transaction.State.INPROGRESS, meta);
    }

    @Override
    public PaypalPayment executePaypalTransaction(String sku, String payerId) {
        ObjectNode meta = Json.newObject();
        return new PaypalPayment("PAY-TEST-SKU", "TOPUP TEST", Transaction.State.APPROVED, meta);
    }

}
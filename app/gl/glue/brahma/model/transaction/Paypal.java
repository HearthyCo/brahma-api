package gl.glue.brahma.model.transaction;

import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import java.util.Date;

public class Paypal extends Transaction {

    public enum State { CREATED, APPROVED, FAILED, CANCELED, EXPIRED, PENDING }
    public enum Intent { SALE, AUTHORIZE, ORDER }

    @NotNull
    private String pay_id;

    private Date create_time;

    private Date update_time;

    @Enumerated(EnumType.STRING)
    @NotNull
    private State state;

    @Enumerated(EnumType.STRING)
    @NotNull
    private Intent intent;

    @NotNull // Fake to prevent typing bug
    private String payer = "{}";

    @Transient
    private JsonNode payerParsed; // Cache for payer parsing

    @NotNull // Fake to prevent typing bug
    private String transactions = "[]";

    @Transient
    private JsonNode transactionsParsed; // Cache for transactions parsing

    @NotNull // Fake to prevent typing bug
    private String links = "[]";

    @Transient
    private JsonNode linksParsed; // Cache for links parsing


    public String getPayId() {
        return pay_id;
    }

    public void setPayId(String payId) {
        this.pay_id = payId;
    }

    public Date getCreate_time() {
        return create_time;
    }

    public void setCreate_time(Date createTime) {
        this.create_time = createTime;
    }

    public Date getUpdate_time() {
        return update_time;
    }

    public void setUpdate_time(Date updateTime) {
        this.update_time = updateTime;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public Intent getIntent() {
        return intent;
    }

    public void setIntent(Intent intent) {
        this.intent = intent;
    }

    public JsonNode getPayer(){
        if (payerParsed == null) {
            payerParsed = payer == null ? Json.newObject() : Json.parse(payer);
        }
        return payerParsed;
    }

    public void setPayer(JsonNode payer) {
        this.payerParsed = payer;
        this.payer = payer == null ? "{}" : payer.toString();
    }

    public JsonNode getTransactions(){
        if (transactionsParsed == null) {
            transactionsParsed = transactions == null ? Json.newObject() : Json.parse(transactions);
        }
        return transactionsParsed;
    }

    public void setTransactions(JsonNode transactions) {
        this.transactionsParsed = transactions;
        this.transactions = transactions == null ? "[]" : transactions.toString();
    }

    public JsonNode getLinks(){
        if (linksParsed == null) {
            linksParsed = links == null ? Json.newObject() : Json.parse(links);
        }
        return linksParsed;
    }

    public void setLinks(JsonNode payer) {
        this.linksParsed = payer;
        this.links = links == null ? "[]" : links.toString();
    }
}
package gl.glue.brahma.model.transaction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import gl.glue.brahma.model.service.Service;
import gl.glue.brahma.model.session.Session;
import gl.glue.brahma.model.user.User;
import gl.glue.brahma.util.serializers.SessionToTitleSerializer;
import play.libs.Json;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@NamedQueries({

        @NamedQuery(
                name = "Transaction.getBySku",
                query = "select transaction " +
                        "from Transaction transaction " +
                        "where transaction.sku = :sku"
        ),
        @NamedQuery(
                name = "Transaction.getTransactionHistory",
                query = "select transaction " +
                        "from Transaction transaction " +
                        "left join fetch transaction.session session " +
                        "where transaction.user.id = :id " +
                        "and transaction.state = :state " +
                        "order by transaction.timestamp desc"
        ),
        @NamedQuery(
                name = "Transaction.getUserBalance",
                query = "select sum(transaction.amount) " +
                        "from Transaction transaction " +
                        "where transaction.user.id = :id " +
                        "and transaction.state = :state"
        )

})
@Entity
public class Transaction {

    public enum State { INPROGRESS, APPROVED, FAILED }
    public enum Reason { SESSION_PAYMENT, SESSION_EARNINGS, SESSION_DEVOLUTION, TOP_UP, CASH_OUT, ADMIN_ACTION }

    @Id
    @SequenceGenerator(name = "transaction_id_seq", sequenceName = "transaction_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "transaction_id_seq")
    private int id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @NotNull
    @JsonIgnore
    private User user;

    @ManyToOne
    @JoinColumn(name = "session_id")
    @JsonSerialize(using = SessionToTitleSerializer.class, as=Session.class)
    private Session session;

    @NotNull
    private int amount;

    @Enumerated(EnumType.STRING)
    @NotNull
    private State state;

    private String sku;

    @NotNull
    private Date timestamp;

    @Enumerated(EnumType.STRING)
    @NotNull
    private Reason reason;

    @NotNull // Fake to prevent typing bug
    private String meta = "{}";

    @Transient
    private JsonNode metaParsed; // Cache for meta parsing

    public Transaction() { }

    public Transaction(User user, int amount, State state, String sku, Reason reason) {
        this.user = user;
        this.amount = amount;
        this.state = state;
        this.sku = sku;
        this.timestamp = new Date();
        this.reason = reason;
    }

    public Transaction(User user, Session session) {
        this.user = user;
        this.amount = -1 * session.getServiceType().getPrice();
        this.session = session;
        this.state = State.APPROVED;
        this.reason = Reason.SESSION_PAYMENT;
        this.timestamp = new Date();
    }

    public Transaction(User user, Session session, Service service) {
        this.user = user;
        this.amount = service.getEarnings();
        this.session = session;
        this.state = State.APPROVED;
        this.reason = Reason.SESSION_EARNINGS;
        this.timestamp = new Date();
    }

    public int getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public int getAmount() { return amount; }

    public void setAmount(int amount) { this.amount = amount; }

    public State getState() {
        return state;
    }

    public void setState(State state) { this.state = state; }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Reason getReason() {
        return reason;
    }

    public void setReason(Reason reason) {
        this.reason = reason;
    }

    public JsonNode getMeta() {
        if (metaParsed == null) {
            metaParsed = meta == null ? Json.newObject() : Json.parse(meta);
        }
        return metaParsed;
    }

    public void setMeta(JsonNode meta) {
        this.metaParsed = meta;
        this.meta = meta == null ? "{}" : meta.toString();
    }


    @Override
    public String toString() {
        return "Transaction #" + id;
    }
}


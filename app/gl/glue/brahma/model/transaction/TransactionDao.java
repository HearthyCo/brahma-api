package gl.glue.brahma.model.transaction;

import play.db.jpa.JPA;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.List;

public class TransactionDao {


    public void create(Transaction transaction) {
        JPA.em().persist(transaction);
    }

    public Transaction getById(int id) {
        return JPA.em().find(Transaction.class, id);
    }

    public Transaction getBySku(String sku) {
        try {
            return JPA.em().createNamedQuery("Transaction.getBySku", Transaction.class)
                    .setParameter("sku", sku)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<Transaction> getTransactionHistory(int uid, int limit) {
        TypedQuery<Transaction> queryExec = JPA.em()
                .createNamedQuery("Transaction.getTransactionHistory", Transaction.class)
                .setParameter("id", uid)
                .setParameter("state", Transaction.State.APPROVED);

        if (limit > 0) {
            queryExec.setMaxResults(limit);
        }

        return queryExec.getResultList();
    }

    public List<Transaction> getTransactionHistory(int id) {
        return getTransactionHistory(id, 0);
    }

    public int getUserBalance(int uid) {
        try {
            return JPA.em().createNamedQuery("Transaction.getUserBalance", Long.class)
                    .setParameter("id", uid)
                    .setParameter("state", Transaction.State.APPROVED)
                    .getSingleResult()
                    .intValue();
        } catch (NoResultException e) {
            return 0;
        }
    }
}

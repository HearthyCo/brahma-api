package gl.glue.brahma.model.transaction;

import play.db.jpa.JPA;

import javax.persistence.NoResultException;
import java.util.List;

public class TransactionDao {

    public void create(Transaction transaction) {
        JPA.em().persist(transaction);
    }

    public Transaction getById(int id) {
        try {
            String query = "select transaction from Transaction transaction where transaction.id = :id";

            return JPA.em().createQuery(query, Transaction.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public Transaction getBySku(String sku) {
        try {
            String query = "select transaction from Transaction transaction where transaction.sku = :sku";

            return JPA.em().createQuery(query, Transaction.class)
                    .setParameter("sku", sku)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<Transaction> getTransactionHistory(int id) {

        try {
            String query =
                    "select transaction from Transaction transaction " +
                    "left join fetch transaction.session session " +
                    "where transaction.user.id = :id " +
                    "order by transaction.timestamp desc";

            return JPA.em().createQuery(query, Transaction.class)
                    .setParameter("id", id)
                    .getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }
}

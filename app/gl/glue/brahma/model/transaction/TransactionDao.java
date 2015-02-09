package gl.glue.brahma.model.transaction;

import play.db.jpa.JPA;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.List;

public class TransactionDao {

    public List<Transaction> getTransactionHistory(int id, int limit) {

        try {
            String query =
                    "SELECT transaction FROM Transaction transaction " +
                    "LEFT JOIN FETCH transaction.session session " +
                    "WHERE transaction.user.id = :id " +
                    "ORDER BY transaction.timestamp DESC";

            TypedQuery<Transaction> queryExec = JPA.em()
                    .createQuery(query, Transaction.class)
                    .setParameter("id", id);

            if (limit > 0) {
                queryExec.setMaxResults(limit);
            }

            return queryExec.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }
    public List<Transaction> getTransactionHistory(int id) {
        return getTransactionHistory(id, 0);
    }
}

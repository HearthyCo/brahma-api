package gl.glue.brahma.model.transaction;

import play.db.jpa.JPA;

import javax.persistence.NoResultException;
import java.util.List;

public class TransactionDao {

    public List<Transaction> getTransactionHistory(int id) {

        try {
            String query =
                    "select transaction from Transaction transaction " +
                    "left join fetch transaction.session session " +
                    "where transaction.user.id = :id";

            return JPA.em().createQuery(query, Transaction.class)
                    .setParameter("id", id)
                    .getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }
}

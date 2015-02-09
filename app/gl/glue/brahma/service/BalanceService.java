package gl.glue.brahma.service;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gl.glue.brahma.model.session.Session;
import gl.glue.brahma.model.transaction.Transaction;
import gl.glue.brahma.model.transaction.TransactionDao;
import gl.glue.brahma.model.user.User;
import gl.glue.brahma.model.user.UserDao;
import play.libs.Json;

import java.util.List;

public class BalanceService {

    private UserDao userDao = new UserDao();
    private TransactionDao transactionDao = new TransactionDao();

    /**
     * Search in transactions with uid
     * @param uid User id
     * @return {ObjectNode} Balance of user with a current amount and a transaction list
     */
    public ObjectNode getBalance(int uid, int limit) {
        List<Transaction> transactionList = transactionDao.getTransactionHistory(uid, limit);
        ArrayNode transactions = new ArrayNode(JsonNodeFactory.instance);

        if(!transactionList.isEmpty()) {
            for(Transaction transaction : transactionList) {
                ObjectNode transactionObject = (ObjectNode) Json.toJson(transaction);
                Session s = transaction.getSession();
                if (s != null) {
                    transactionObject.put("title", s.getTitle());
                }
                transactions.add(transactionObject);
            }
        }

        ObjectNode result = Json.newObject();
        result.put("amount", getAmount(uid));
        result.put("transactions", transactions);

        return result;
    }

    public ObjectNode getBalance(int uid) {
        return getBalance(uid, 0);
    }

    /**
     * Find user by uid for get current amount
     * @param uid User id
     * @return {int} Current amount
     */
    public int getAmount(int uid) {
        User user = userDao.findById(uid);
        return user.getBalance();
    }
}

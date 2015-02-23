package gl.glue.brahma.controllers.common;

import actions.BasicAuth;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gl.glue.brahma.model.transaction.Transaction;
import gl.glue.brahma.service.TransactionService;
import gl.glue.brahma.util.JsonUtils;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

public class TransactionController extends Controller {

    private static TransactionService transactionService = new TransactionService();

    @BasicAuth
    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public static Result getTransaction(int id) {
        int uid = Integer.parseInt(session("id"));
        Transaction transaction = transactionService.getTransaction(id);
        if (transaction == null || transaction.getUser().getId() != uid) {
            return status(404, JsonUtils.simpleError("404", "Invalid identifier"));
        }

        // Get session with login
        ObjectNode result = Json.newObject();
        result.put("payment", Json.toJson(transaction));

        return ok(result);
    }
}

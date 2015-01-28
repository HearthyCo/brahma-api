package gl.glue.brahma.controllers;

import actions.BasicAuth;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gl.glue.brahma.model.session.Session;
import gl.glue.brahma.model.transaction.Transaction;
import gl.glue.brahma.service.SessionService;
import gl.glue.brahma.service.TransactionService;
import gl.glue.brahma.util.JsonUtils;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

public class TransactionController extends Controller {

    private static TransactionService transactionService = new TransactionService();
    private static SessionService sessionService = new SessionService();

    /**
     * @api {get} /user/balance Balance
     *
     * @apiGroup User
     * @apiName GetBalance
     * @apiDescription Return a state of user balance in addition also return a history os transactions
     *
     * @apiSuccess {object}      balance                 Contains a balance of user
     * @apiSuccess {int}         balance.amount          Current amount balance of user
     * @apiSuccess {object[]}    balance.transactions    List of all transactions of user
     * @apiSuccessExample {json} Success-Response
     *      HTTP/1.1 200 OK
     *      {
     *          "balance": {
     *              "amount": 20000000,
     *              "transactions": [
     *                  {
     *                      "id": 91300,
     *                      "amount": -1000,
     *                      "timestamp": 1418626800000,
     *                      "reason": "Reserva de sesión",
     *                      "title": "testSession1"
     *                  }
     *              ]
     *          }
     *      }
     *
     * @apiError {Object} UserNotLoggedIn User is not logged in.
     * @apiErrorExample {json} UserNotLoggedIn
     *      HTTP/1.1 401 Unauthorized
     *      {
     *          "status": "401",
     *          "title": "You are not logged in"
     *      }
     *
     * @apiVersion 0.1.0
     */
    @BasicAuth
    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public static Result getBalance() {
        int uid = Integer.parseInt(session("id"));

        // Get session with login
        ObjectNode balance = transactionService.getBalance(uid);

        ObjectNode result = Json.newObject();
        result.put("balance", balance);

        return ok(result);
    }

    // Huerfana de test y sin probar.
    @BasicAuth
    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public static Result createTransaction() {
        int uid = Integer.parseInt(session("id"));

        JsonNode json = request().body().asJson();

        ObjectNode params = JsonUtils.checkRequiredFields(json, "amount");
        if (params != null) return badRequest(params);

        int amount = json.findPath("amount").asInt();

        Transaction transaction = null;
        if(json.has("session")) {
            Session session = sessionService.getById(uid, json.findPath("session").asInt());
            transaction = transactionService.createTransaction(uid, amount, session);
        }
        else {
            transaction = transactionService.createTransaction(uid, amount);
        }

        // Get session with login
        ObjectNode result = Json.newObject();
        result.put("transaction", Json.toJson(transaction));

        return ok(result);
    }
}
package gl.glue.brahma.controllers;

import actions.BasicAuth;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gl.glue.brahma.model.transaction.Transaction;
import gl.glue.brahma.service.TransactionService;
import gl.glue.brahma.util.JsonUtils;
import play.Logger;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import java.util.Map;

public class TransactionController extends Controller {

    private static TransactionService transactionService = new TransactionService();

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

    @BasicAuth
    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public static Result getTransaction(int id) {
        int uid = Integer.parseInt(session("id"));

        Transaction transaction = transactionService.getTransaction(uid, id);

        // Get session with login
        ObjectNode result = Json.newObject();
        result.put("payment", Json.toJson(transaction));

        return ok(result);
    }

    // Huerfana de test y sin probar.
    @BasicAuth
    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public static Result createPaypalTransaction() {
        int uid = Integer.parseInt(session("id"));

        JsonNode json = request().body().asJson();

        Logger.info("JSON " + json);

        ObjectNode params = JsonUtils.checkRequiredFields(json, "amount");
        if (params != null) return badRequest(params);

        int amount = json.findPath("amount").asInt();

        Transaction transaction;
        transaction = transactionService.createPaypalTransaction(uid, amount);

        // Get session with login
        ObjectNode result = Json.newObject();
        result.put("transaction", Json.toJson(transaction));

        return ok(result);
    }

    @BasicAuth
    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public static Result paypalSuccess() {
        Map<String, String[]> args1 = request().queryString();
        Map<String, Object> args = Http.Context.current().args;

        Logger.info("ARGS " + args.toString());
        Logger.info("ARGS1 " + args1.toString());

        String paymentId = String.valueOf(args.get("paymentId"));
        String token = String.valueOf(args.get("token"));
        String payerId = String.valueOf(args.get("PayerID"));

        Logger.info("PAYER " + payerId);
        Logger.info("TOKEN " + token);
        Logger.info("PAYMENT " + paymentId);

        return ok();
    }

    @BasicAuth
    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public static Result paypalCancel() {
        return ok();
    }
}
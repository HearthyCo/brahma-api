package gl.glue.brahma.controllers;

import actions.BasicAuth;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import gl.glue.brahma.model.transaction.Transaction;
import gl.glue.brahma.service.TransactionService;
import gl.glue.brahma.util.JsonUtils;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

import java.net.MalformedURLException;
import java.net.URL;

public class TransactionController extends Controller {

    private static TransactionService transactionService = new TransactionService();
    private static Config conf = ConfigFactory.load();

    @BasicAuth
    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public static Result getTransaction(int id) {
        Transaction transaction = transactionService.getTransaction(id);

        // Get session with login
        ObjectNode result = Json.newObject();
        result.put("payment", Json.toJson(transaction));

        return ok(result);
    }

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
    public static Result getUserTransactions() {
        int uid = Integer.parseInt(session("id"));

        // Get session with login
        ObjectNode balance = transactionService.getUserTransactions(uid);

        ObjectNode result = Json.newObject();
        result.put("balance", balance);

        return ok(result);
    }

    @BasicAuth
    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public static Result createPaypalTransaction() {
        int uid = Integer.parseInt(session("id"));

        JsonNode json = request().body().asJson();

        ObjectNode result = JsonUtils.checkRequiredFields(json, "amount");
        if (result != null) return badRequest(result);

        int amount = json.findPath("amount").asInt();

        String baseUrl = conf.getString("cors.origin") + "/#transaction/url";
        if (request().hasHeader("Referer")) {
            try {
                URL url = new URL(request().getHeader("Referer"));
                baseUrl = url.getProtocol() + "://" + url.getAuthority() + "/#transaction/url";
            } catch (MalformedURLException e) {}
        }

        Transaction transaction;
        transaction = transactionService.createPaypalTransaction(uid, amount, baseUrl);

        // Get session with login
        result = Json.newObject();
        result.put("transaction", Json.toJson(transaction));

        return ok(result);
    }

    @BasicAuth
    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public static Result executePaypalTransaction() {

        JsonNode json = request().body().asJson();

        ObjectNode result = JsonUtils.checkRequiredFields(json, "paymentId", "token", "PayerID");
        if (result != null) return badRequest(result);

        String paypalId = json.findPath("paymentId").asText();
        String token = json.findPath("token").asText();
        String payerId = json.findPath("PayerID").asText();

        Transaction transaction;
        transaction = transactionService.executePaypalTransaction(token, paypalId, payerId);

        if(transaction == null) return status(412, JsonUtils.simpleError("412", "Transaction is not executable"));

        // Get session with login
        result = Json.newObject();
        result.put("transaction", Json.toJson(transaction));
        result.put("balance", transaction.getUser().getBalance());

        return ok(result);
    }
}
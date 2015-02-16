package gl.glue.brahma.controllers.client;

import actions.ClientAuth;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import gl.glue.brahma.model.transaction.Transaction;
import gl.glue.brahma.service.TransactionService;
import gl.glue.brahma.service.UserService;
import gl.glue.brahma.util.JsonUtils;
import play.Logger;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

import java.net.MalformedURLException;
import java.net.URL;

public class TransactionController extends Controller {

    private static UserService userService = new UserService();
    private static TransactionService transactionService = new TransactionService();
    private static Config conf = ConfigFactory.load();

    /**
     * @api {get} /client/me/balance Balance
     *
     * @apiGroup Client
     * @apiName GetBalance
     * @apiDescription Return a state of user balance in addition also return a history os transactions
     *
     * @apiSuccess {int}         amount                  Current user balance
     * @apiSuccess {object[]}    transactions            List of all transactions of user
     * @apiSuccessExample {json} Success-Response
     *      HTTP/1.1 200 OK
     *      {
     *          "balance": 20000000,
     *          "transactions": [
     *              {
     *                  "id": 91300,
     *                  "amount": -1000,
     *                  "timestamp": 1418626800000,
     *                  "reason": "Reserva de sesión",
     *                  "title": "testSession1"
     *              }
     *          ]
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
     * @apiError {Object} UserUnauthorized User is not a client
     * @apiErrorExample {json} UserUnauthorized
     *      HTTP/1.1 403 Unauthorized
     *      {
     *          "status": "403",
     *          "title": "Unauthorized"
     *      }
     *
     * @apiVersion 0.1.0
     */
    @ClientAuth
    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public static Result getUserTransactions() {
        int uid = Integer.parseInt(session("id"));

        ObjectNode result = Json.newObject();
        result.put("balance", userService.getById(uid).getBalance());
        result.put("transactions", Json.toJson(transactionService.getUserTransactions(uid)));

        return ok(result);
    }

    /**
     * @api {post} /transaction CreateTransaction
     *
     * @apiGroup Transaction
     * @apiName CreatePaypalTransaction
     * @apiDescription Create a new Paypal transaction and return info about it.
     *
     * @apiParam {int}    amount    The amount of money to spend, in cents.
     * @apiParamExample {json} Request-Example
     *      {
     *          "amount": "1000"
     *      }
     *
     * @apiSuccess {object}      transaction    The newly created Paypal transaction
     * @apiSuccess {string}      redirect       The external URL to complete the payment
     * @apiSuccessExample {json} Success-Response
     *      HTTP/1.1 200 OK
     *      {
     *          "transaction": {
     *              "id": 5,
     *              "amount": 1000,
     *              "state": "INPROGRESS",
     *              "sku": "PAY-6KM12958S00676842KTMOFLI",
     *              "timestamp": 1423499949564,
     *              "reason": "Topup your account",
     *              "meta": {}
     *          },
     *          "redirect": "https://www.sandbox.paypal.com/cgi-bin/webscr?cmd=_express-checkout&token=EC-8DJ03018CJ419243D"
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
     * @apiError {Object} UserUnauthorized User is not a client
     * @apiErrorExample {json} UserUnauthorized
     *      HTTP/1.1 403 Unauthorized
     *      {
     *          "status": "403",
     *          "title": "Unauthorized"
     *      }
     *
     * @apiVersion 0.1.0
     */
    @ClientAuth
    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public static Result createPaypalTransaction() {
        int uid = Integer.parseInt(session("id"));

        JsonNode json = request().body().asJson();

        ObjectNode result = JsonUtils.checkRequiredFields(json, "amount");
        if (result != null) return badRequest(result);

        int amount = json.findPath("amount").asInt();

        ObjectNode redirectUrls;
        if(json.has("redirectUrls")) {
            redirectUrls = (ObjectNode) json.findPath("redirectUrls");
        }
        else {
            String baseUrl = conf.getString("cors.origin") + "/#transaction/url";
            redirectUrls = Json.newObject();
            if (request().hasHeader("Referer")) {
                try {
                    URL url = new URL(request().getHeader("Referer"));
                    baseUrl = url.getProtocol() + "://" + url.getAuthority() + "/#transaction/url";
                } catch (MalformedURLException e) {
                }
            }
            redirectUrls.put("success", baseUrl + "/success");
            redirectUrls.put("cancel", baseUrl + "/cancel");
        }

        Transaction transaction;
        transaction = transactionService.createPaypalTransaction(uid, amount, redirectUrls);

        // Put the transaction in the response
        result = Json.newObject();
        result.put("transaction", Json.toJson(transaction));

        // Copy the paypal approval URL to the redirect field
        String redir = transaction.getMeta().get("paypal").get("links").get(1).get("href").asText();
        result.put("redirect", redir);

        return ok(result);
    }


    /**
     * @api {post} /transaction/execute ExecuteTransaction
     *
     * @apiGroup Transaction
     * @apiName ExecutePaypalTransaction
     * @apiDescription Completes a Paypal transaction.
     *
     * @apiParam {string}    PayerID      The PayerID returned by Paypal after approving the transaction
     * @apiParam {string}    paymentId    The paymentId returned by Paypal after approving the transaction
     * @apiParam {string}    token        The token returned by Paypal after approving the transaction
     * @apiParamExample {json} Request-Example
     *      {
     *          "PayerID": "M8PEV8LHANHJY"
     *          "paymentId": "PAY-98B38881S9937240WKTMOI6Q"
     *          "token": "EC-0HH25056CY103791W"
     *      }
     *
     * @apiSuccess {object}      transaction    The completed Paypal transaction
     * @apiSuccess {int}         balance        The updated user balance after the transaction
     * @apiSuccessExample {json} Success-Response
     *      HTTP/1.1 200 OK
     *      {
     *          "transaction": {
     *              "id": 5,
     *              "amount": 1000,
     *              "state": "APPROVED",
     *              "sku": "PAY-6KM12958S00676842KTMOFLI",
     *              "timestamp": 1423499949564,
     *              "reason": "Topup your account",
     *              "meta": {}
     *          },
     *          "balance": 1000
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
     * @apiError {Object} TransactionInvalid Transaction cannot be executed.
     * @apiErrorExample {json} TransactionInvalid
     *      HTTP/1.1 412 Precondition failed
     *      {
     *          "status": "412",
     *          "title": "Transaction is not executable"
     *      }
     *
     * @apiError {Object} UserUnauthorized User is not a client
     * @apiErrorExample {json} UserUnauthorized
     *      HTTP/1.1 403 Unauthorized
     *      {
     *          "status": "403",
     *          "title": "Unauthorized"
     *      }
     *
     * @apiVersion 0.1.0
     */
    @ClientAuth
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

        Logger.info("RESULT " + result);

        return ok(result);
    }
}
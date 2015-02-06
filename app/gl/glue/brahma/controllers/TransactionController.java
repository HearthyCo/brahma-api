package gl.glue.brahma.controllers;

import actions.BasicAuth;
import com.fasterxml.jackson.databind.JsonNode;
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
        Transaction transaction = transactionService.getTransaction(id);

        // Get session with login
        ObjectNode result = Json.newObject();
        result.put("payment", Json.toJson(transaction));

        return ok(result);
    }

    @BasicAuth
    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public static Result createPaypalTransaction() {
        int uid = Integer.parseInt(session("id"));

        JsonNode json = request().body().asJson();

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
    public static Result executePay() {

        JsonNode json = request().body().asJson();

        ObjectNode params = JsonUtils.checkRequiredFields(json, "paymentId", "token", "PayerID");
        if (params != null) return badRequest(params);

        String paypalId = json.findPath("paymentId").asText();
        String token = json.findPath("token").asText();
        String payerId = json.findPath("PayerID").asText();

        Transaction transaction;
        transaction = transactionService.executePay(token, paypalId, payerId);

        // Get session with login
        ObjectNode result = Json.newObject();
        result.put("transaction", Json.toJson(transaction));

        return ok();
    }
}
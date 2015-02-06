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
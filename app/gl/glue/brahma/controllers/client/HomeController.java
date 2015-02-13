package gl.glue.brahma.controllers.client;

import actions.BasicAuth;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gl.glue.brahma.service.SessionService;
import gl.glue.brahma.service.TransactionService;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

public class HomeController extends Controller {

    private static SessionService sessionService = new SessionService();
    private static TransactionService transactionService = new TransactionService();

    /**
     * @api {get} /user/home Homepage
     *
     * @apiGroup User
     * @apiName GetHome
     * @apiDescription Collect all entities required to show in home view.
     *
     * @apiSuccess {Object}     sessions             Contains all user sessions grouped by state.
     * @apiSuccess {Object[]}   sessions.programmed  Contents all user sessions in programmed state.
     * @apiSuccess {Object[]}   sessions.underway    Contents all user sessions in underway state.
     * @apiSuccess {Object[]}   sessions.closed      Contents all user sessions in closed state.
     * @apiSuccess {Object}     balance              Balance.
     * @apiSuccess {Float}      balance.amount       Current balance.
     * @apiSuccess {Object[]}   balance.transactions Most recent transactions.
     * @apiSuccessExample {json} Success-Response
     *      HTTP/1.1 200 OK
     *      {
     *          "sessions": {
     *              "programmed": [
     *                  {
     *                      "id": 90700,
     *                      "title": "testSession1",
     *                      "startDate": 1425312000000,
     *                      "endDate": 1425312900000,
     *                      "state": "PROGRAMMED",
     *                      "meta": {},
     *                      "timestamp": 1418626800000,
     *                      "isNew": true
     *                    }
     *              ],
     *              "underway": [],
     *              "closed": [
     *                  {
     *                      "id": 90702,
     *                      "title": "testSession3",
     *                      "startDate": 1425384000000,
     *                      "endDate": 1425208500000,
     *                      "state": "CLOSED",
     *                      "meta": {},
     *                      "timestamp": 1418666400000,
     *                      "isNew": true
     *                  }
     *              ]
     *          },
     *          "balance": {
     *              amount: 1000,
     *              transactions: [
     *                  {
     *                      "id": 10001,
     *                      "amount": -1000,
     *                      "timestamp": 1418626800000,
     *                      "reason": "Reserva de sesión",
     *                      "title": "testSession1"
     *                  },
     *                  {
     *                      "id": 10005,
     *                      "amount": 2000,
     *                      "timestamp": 1418619600000,
     *                      "reason": "Incremento de saldo",
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
    public static Result get() {
        int uid = Integer.parseInt(session("id"));

        // Get sessions of user with login
        ObjectNode sessions = sessionService.getUserSessions(uid);
        ObjectNode transactions = transactionService.getUserTransactions(uid, 2);

        ObjectNode result = Json.newObject();
        result.put("sessions", sessions);
        result.put("balance", transactions);

        return ok(result);
    }
}

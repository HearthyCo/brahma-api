package gl.glue.brahma.controllers.client;

import actions.ClientAuth;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gl.glue.brahma.model.session.Session;
import gl.glue.brahma.model.sessionuser.SessionUser;
import gl.glue.brahma.model.user.User;
import gl.glue.brahma.service.SessionService;
import gl.glue.brahma.service.TransactionService;
import gl.glue.brahma.service.UserService;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class HomeController extends Controller {

    private static final int MAX_RESULTS = 2;
    private static UserService userService = new UserService();
    private static SessionService sessionService = new SessionService();
    private static TransactionService transactionService = new TransactionService();

    /**
     * @api {get} /client/me/home Homepage
     *
     * @apiGroup Client
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
     * @apiError {Object} UserUnauthorizedUser User is not a client.
     * @apiErrorExample {json} UserUnauthorizedUser
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
    public static Result getHome() {
        int uid = Integer.parseInt(session("id"));

        User user = userService.getById(uid);

        // Create State Session List Array for iterate and pass DAO function a Session.State ArrayList
        List<Set<Session.State>> states = new ArrayList<>();
        String[] listStates = { "programmed", "underway", "closed" };

        states.add(EnumSet.of(Session.State.PROGRAMMED));
        states.add(EnumSet.of(Session.State.UNDERWAY));
        states.add(EnumSet.of(Session.State.CLOSED, Session.State.FINISHED));

        ObjectNode sessions = Json.newObject();

        // Iterate State Session List Array
        for (Set<Session.State> state : states) {
            List<SessionUser> sessionUsers = sessionService.getUserSessionsByState(uid, state);

            ArrayNode sessionsState = new ArrayNode(JsonNodeFactory.instance);
            for(SessionUser sessionUser : sessionUsers) {
                Session session = sessionUser.getSession();

                boolean isNew = true;
                if(sessionUser.getViewedDate() != null) {
                    isNew = session.getTimestamp().after(sessionUser.getViewedDate());
                }

                ObjectNode sessionObject = (ObjectNode) Json.toJson(session);
                sessionObject.put("isNew", isNew);

                sessionsState.add(sessionObject);
            }

            sessions.put(listStates[states.indexOf(state)], sessionsState);
        }

        return ok(Json.newObject()
                .putPOJO("sessions", sessions)
                .put("balance", Json.newObject()
                        .put("balance", user.getBalance())
                        .put("transactions", Json.toJson(transactionService.getUserTransactions(uid, MAX_RESULTS)))));

    }
}

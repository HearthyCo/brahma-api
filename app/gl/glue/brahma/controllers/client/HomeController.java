package gl.glue.brahma.controllers.client;

import actions.ClientAuth;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gl.glue.brahma.model.session.Session;
import gl.glue.brahma.model.sessionuser.SessionUser;
import gl.glue.brahma.model.transaction.Transaction;
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
import java.util.stream.Collectors;

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
     *          "home": {
     *              "sessions": {
     *                  "programmed": [90700, 90704],
     *                  "underway": [90712],
     *                  "closed": [90702, 90703]
     *              },
     *              "transactions": [91303, 91302]
     *          },
     *          "sessions": [
     *              {
     *                  "id": 90700,
     *                  "title": "testSession1",
     *                  "startDate": 1425312000000,
     *                  "endDate": 1425312900000,
     *                  "state": "PROGRAMMED",
     *                  "meta": {},
     *                  "timestamp": 1418626800000,
     *                  "isNew": true
     *              },
     *              {
     *                  "id": 90704,
     *                  "title": "testSession5",
     *                  "startDate": 1425571200000,
     *                  "endDate": 1426176900000,
     *                  "state": "PROGRAMMED",
     *                  "meta": {},
     *                  "timestamp": 1418626800000,
     *                  "isNew": true
     *              },
     *              {
     *                  "id": 90712,
     *                  "title": "testPool1",
     *                  "startDate": 1423670400000,
     *                  "endDate": 1423671300000,
     *                  "state": "REQUESTED",
     *                  "meta": {},
     *                  "timestamp": 1418626800000,
     *                  "isNew": true
     *              },
     *              {
     *                  "id": 90702,
     *                  "title": "testSession3",
     *                  "startDate": 1425384000000,
     *                  "endDate": 1425208500000,
     *                  "state": "CLOSED",
     *                  "meta": {},
     *                  "timestamp": 1418666400000,
     *                  "isNew": true
     *              },
     *              {
     *                  "id": 90703,
     *                  "title": "testSession4",
     *                  "startDate": 1425474000000,
     *                  "endDate": 1425208500000,
     *                  "state": "FINISHED",
     *                  "meta": {},
     *                  "timestamp": 1418641200000,
     *                  "isNew": true
     *              }
     *          ],
     *          "transactions": [
     *              {
     *                  "id": 91303,
     *                  "amount": 1000,
     *                  "state": "APPROVED",
     *                  "sku": "TOPUPPPL_000000090000_0000001423154296",
     *                  "timestamp": 1418641200000,
     *                  "reason": "Devolución sesión cancelada",
     *                  "meta": {},
     *                  "session": "testSession2"
     *              },
     *              {
     *                  "id": 91302,
     *                  "amount": -1000,
     *                  "state": "APPROVED",
     *                  "sku": "TOPUPPPL_000000090000_0000001423154295",
     *                  "timestamp": 1418630400000,
     *                  "reason": "Reserva de sesión",
     *                  "meta": {},
     *                  "session": "testSession2"
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
        String[] listStates = { "programmed", "underway", "closed" };
        List<Set<Session.State>> states = new ArrayList<>();
        states.add(EnumSet.of(Session.State.PROGRAMMED));
        states.add(EnumSet.of(Session.State.REQUESTED, Session.State.UNDERWAY));
        states.add(EnumSet.of(Session.State.CLOSED, Session.State.FINISHED));

        ArrayNode sessions = new ArrayNode(JsonNodeFactory.instance);
        ObjectNode sessionsByState = Json.newObject();

        // Iterate State Session List Array
        for (Set<Session.State> state : states) {
            List<SessionUser> sessionUsers = sessionService.getUserSessionsByState(uid, state);

            ArrayNode thisState = new ArrayNode(JsonNodeFactory.instance);
            for(SessionUser sessionUser : sessionUsers) {
                Session session = sessionUser.getSession();

                boolean isNew = true;
                if(sessionUser.getViewedDate() != null) {
                    isNew = session.getTimestamp().after(sessionUser.getViewedDate());
                }

                ObjectNode sessionObject = (ObjectNode) Json.toJson(session);
                sessionObject.put("isNew", isNew);
                sessions.add(sessionObject);

                thisState.add(session.getId());
            }

            sessionsByState.put(listStates[states.indexOf(state)], thisState);
        }

        List<Transaction> transactions = transactionService.getUserTransactions(uid, MAX_RESULTS);
        List<Integer> transactionIds = transactions.stream().map(o -> o.getId()).collect(Collectors.toList());

        return ok(Json.newObject()
                .putPOJO("home", Json.newObject()
                        .putPOJO("sessions", sessionsByState)
                        .putPOJO("transactions", Json.toJson(transactionIds)))
                .putPOJO("sessions", sessions)
                .putPOJO("transactions", Json.toJson(transactions)));

    }
}

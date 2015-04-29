package gl.glue.brahma.controllers.client;

import actions.ClientAuth;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.wordnik.swagger.annotations.*;
import gl.glue.brahma.model.session.Session;
import gl.glue.brahma.model.sessionuser.SessionUser;
import gl.glue.brahma.model.transaction.Transaction;
import gl.glue.brahma.model.user.User;
import gl.glue.brahma.service.ServiceService;
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

@Api(value = "/client", description = "User client functions")
public class HomeController extends Controller {

    private static final int MAX_RESULTS = 2;
    private static UserService userService = new UserService();
    private static SessionService sessionService = new SessionService();
    private static ServiceService serviceService = new ServiceService();
    private static TransactionService transactionService = new TransactionService();

    @ApiOperation(nickname = "getHome", value = "Get client home",
            notes = "Returns the info required to show a client's home page.",
            httpMethod = "GET")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "User is not logged in."),
            @ApiResponse(code = 403, message = "Unauthorized") })
    @ClientAuth
    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public static Result getHome() {
        int uid = Integer.parseInt(session("id"));

        User user = userService.getById(uid);

        // Create State Session List Array for iterate and pass DAO function a Session.State ArrayList
        // String[] listStates = { "programmed", "underway", "closed" };
        String[] listStates = { "underway", "closed" };
        List<Set<Session.State>> states = new ArrayList<>();
        // states.add(EnumSet.of(Session.State.PROGRAMMED));
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
                .putPOJO("transactions", Json.toJson(transactions))
                .putPOJO("servicetypes", Json.toJson(serviceService.getAllServiceTypes()))
                .putPOJO("users", new ArrayNode(JsonNodeFactory.instance).add(Json.toJson(user))));

    }
}

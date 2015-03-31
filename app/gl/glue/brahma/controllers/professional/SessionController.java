package gl.glue.brahma.controllers.professional;

import actions.ProfessionalAuth;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.wordnik.swagger.annotations.*;
import gl.glue.brahma.model.historyentry.HistoryEntry;
import gl.glue.brahma.model.service.Service;
import gl.glue.brahma.model.servicetype.ServiceType;
import gl.glue.brahma.model.session.Session;
import gl.glue.brahma.model.sessionuser.SessionUser;
import gl.glue.brahma.model.user.User;
import gl.glue.brahma.service.HistoryService;
import gl.glue.brahma.service.ServiceService;
import gl.glue.brahma.service.SessionService;
import gl.glue.brahma.util.JsonUtils;
import gl.glue.brahma.util.SignatureHelper;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

import javax.ws.rs.PathParam;
import java.util.*;
import java.util.stream.Collectors;

@Api(value = "/professional", description = "Professional functions")
public class SessionController extends Controller {

    private static SessionService sessionService = new SessionService();
    private static ServiceService serviceService = new ServiceService();
    private static HistoryService historyService = new HistoryService();


    @ApiOperation(nickname = "getAssignedSessions", value = "Get assigned Sessions",
            notes = "Returns the currently assigned sessions.",
            httpMethod = "GET")
    @ApiImplicitParams(value= {
            @ApiImplicitParam(name = "body", defaultValue = "{\n" +
                    "  \"serviceType\": \"90302\"\n" +
                    "}", value="Object with post params", required = true, dataType = "Object", paramType = "body")})
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Missing required field \"serviceType\""),
            @ApiResponse(code = 401, message = "You are not logged in"),
            @ApiResponse(code = 404, message = "Couldn't assign any session") })
    @ProfessionalAuth
    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public static Result assignSessionFromPool() {
        int uid = Integer.parseInt(session("id"));

        JsonNode json = request().body().asJson();
        if (!json.has("serviceType")) {
            return badRequest(JsonUtils.missingRequiredField("serviceType"));
        }
        int serviceTypeId = json.get("serviceType").asInt();

        Session session = sessionService.assignSessionFromPool(uid, serviceTypeId);
        if (session == null) {
            return notFound(JsonUtils.simpleError("404", "Couldn't assign any session"));
        }

        return getAssignedSessions();
    }


    @ApiOperation(nickname = "getAssignedSessions", value = "Get assigned Sessions",
            notes = "Returns the currently assigned sessions.",
            httpMethod = "GET")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "You are not logged in") })
    @ProfessionalAuth
    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public static Result getAssignedSessions() {
        int uid = Integer.parseInt(session("id"));

        // We list only the service types we offer, as we can't accept sessions of other types.
        List<Service> services = serviceService.getServicesOfUser(uid);
        List<ServiceType> acceptableServiceTypes =
                services.stream().map(o -> o.getServiceType()).collect(Collectors.toList());

        Map<Integer, Integer> poolsSize = sessionService.getPoolsSize();
        ArrayNode serviceTypes = new ArrayNode(JsonNodeFactory.instance);
        for (ServiceType serviceType: acceptableServiceTypes) {
            ObjectNode serviceTypeObject = (ObjectNode) Json.toJson(serviceType);
            int queue = poolsSize.containsKey(serviceType.getId()) ? poolsSize.get(serviceType.getId()) : 0;
            serviceTypeObject.put("waiting", queue);
            serviceTypes.add(serviceTypeObject);
        }

        // Get all our sessions.
        Set<Session.State> states = EnumSet.of(Session.State.UNDERWAY, Session.State.CLOSED);
        List<SessionUser> sessionUsers = sessionService.getUserSessionsByState(uid, states, -1);

        ArrayNode sessions = new ArrayNode(JsonNodeFactory.instance);
        ObjectNode sessionsByServiceType = Json.newObject();

        for (SessionUser sessionUser: sessionUsers) {
            ServiceType serviceType = sessionUser.getSession().getServiceType();
            String serviceTypeId = Integer.toString(serviceType.getId());
            if (!sessionsByServiceType.has(serviceTypeId))
                sessionsByServiceType.put(serviceTypeId, new ArrayNode(JsonNodeFactory.instance));
            sessions.add(Json.toJson(sessionUser.getSession()));
            ((ArrayNode)sessionsByServiceType.get(serviceTypeId)).add(Json.toJson(sessionUser.getSession()));
            // If we can't offer this servicetype, show it anyway (we've been invited?)
            if (!acceptableServiceTypes.contains(serviceType)) {
                acceptableServiceTypes.add(serviceType);
                serviceTypes.add(Json.toJson(serviceType));
            }
        }

        ObjectNode result = Json.newObject()
                .putPOJO("serviceTypeSessions", sessionsByServiceType)
                .putPOJO("sessions", sessions)
                .putPOJO("servicetypes", serviceTypes);
        SignatureHelper.addSignatures(result, uid);
        return ok(result);
    }


    @ApiOperation(nickname = "getPoolsSize", value = "Get Pools Size",
            notes = "Returns the current queue size for each pool. Pools without queue are ignored.",
            httpMethod = "GET")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "You are not logged in") })
    @ProfessionalAuth
    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public static Result getPoolsSize() {
        ObjectNode ret = Json.newObject();
        ret.put("pools", Json.toJson(sessionService.getPoolsSize()));
        return ok(ret);
    }


    @ApiOperation(nickname = "getSession", value = "Get Session",
            notes = "Collect info  on a session and its participants.", httpMethod = "GET")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "You are not logged in"),
            @ApiResponse(code = 404, message = "Invalid identifier") })
    @ProfessionalAuth
    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public static Result getSession(@ApiParam(value = "Session id", required = true) @PathParam("session") int id) {
        int uid = Integer.parseInt(session("id"));
        Session session = sessionService.getById(id, uid);
        if (session == null) return status(404, JsonUtils.simpleError("404", "Invalid identifier"));

        List<SessionUser> sessionUsers = sessionService.getSessionUsers(id);
        List<Integer> participants = new ArrayList<>();
        List<User> users = new ArrayList<>();
        List<HistoryEntry> historyEntries = new ArrayList<>();
        ObjectNode userHistoryEntries = Json.newObject();

        for (SessionUser sessionUser: sessionUsers) {
            User u = sessionUser.getUser();
            if (u.getId() == uid) {
                sessionUser.setViewedDate(new Date());
            }
            participants.add(sessionUser.getId());
            users.add(u);
            if (u.getUserType().equals("client")) {
                List<HistoryEntry> userHistory = historyService.getHistory(u.getId());
                List<Integer> userHistoryIds = userHistory.stream().map(o -> o.getId()).collect(Collectors.toList());
                historyEntries.addAll(userHistory);
                userHistoryEntries.put(Integer.toString(u.getId()), Json.toJson(userHistoryIds));
            }
        }

        return ok(Json.newObject()
            .putPOJO("sessions", new ArrayNode(JsonNodeFactory.instance).add(Json.toJson(session)))
            .putPOJO("users", Json.toJson(users))
            .putPOJO("sessionusers", Json.toJson(sessionUsers))
            .putPOJO("participants", Json.newObject().putPOJO(Integer.toString(id), Json.toJson(participants)))
            .putPOJO("historyentries", Json.toJson(historyEntries))
            .putPOJO("userHistoryEntries", userHistoryEntries));
    }


    @ApiOperation(nickname = "closeSession", value = "Close Session",
            notes = "Closes the specified session", httpMethod = "POST")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "You are not logged in"),
            @ApiResponse(code = 404, message = "Invalid identifier"),
            @ApiResponse(code = 409, message = "Session in wrong state") })
    @ProfessionalAuth
    @Transactional
    public static Result closeSession(@ApiParam(value = "Session id", required = true) @PathParam("session") int id) {
        int uid = Integer.parseInt(session("id"));
        Session session = sessionService.close(id, uid);
        if (session == null) return status(404, JsonUtils.simpleError("404", "Invalid identifier"));
        if (session.getState() != Session.State.CLOSED)
            return status(409, JsonUtils.simpleError("409", "Only underway sessions can be closed"));
        return ok(Json.newObject()
                .putPOJO("sessions", new ArrayNode(JsonNodeFactory.instance)
                        .addPOJO(Json.toJson(session))));
    }

}

package gl.glue.brahma.service;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.opentok.OpenTok;
import com.opentok.TokenOptions;
import com.opentok.exception.OpenTokException;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import gl.glue.brahma.exceptions.InvalidStateException;
import gl.glue.brahma.exceptions.TargetNotFoundException;
import gl.glue.brahma.model.servicetype.ServiceType;
import gl.glue.brahma.model.servicetype.ServiceTypeDao;
import gl.glue.brahma.model.service.Service;
import gl.glue.brahma.model.service.ServiceDao;
import gl.glue.brahma.model.session.Session;
import gl.glue.brahma.model.session.SessionDao;
import gl.glue.brahma.model.sessionuser.SessionUser;
import gl.glue.brahma.model.sessionuser.SessionUserDao;
import gl.glue.brahma.model.transaction.Transaction;
import gl.glue.brahma.model.transaction.TransactionDao;
import gl.glue.brahma.model.user.User;
import gl.glue.brahma.model.user.UserDao;
import gl.glue.brahma.util.RedisHelper;
import gl.glue.play.amqp.Controller;
import play.Configuration;
import play.Play;
import play.db.jpa.Transactional;
import play.libs.Json;
import redis.clients.jedis.Jedis;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class SessionService {

    private static Config conf = ConfigFactory.load();

    private static final int MAX_RESULTS = 2;
    private UserDao userDao = new UserDao();
    private SessionDao sessionDao = new SessionDao();
    private SessionUserDao sessionUserDao = new SessionUserDao();
    private ServiceDao serviceDao = new ServiceDao();
    private ServiceTypeDao serviceTypeDao = new ServiceTypeDao();
    private TransactionDao transactionDao = new TransactionDao();
    private RedisHelper redisHelper = new RedisHelper();

    private OpenTok openTok;

    public SessionService() {
        Configuration configuration = Play.application().configuration();
        int key = configuration.getInt("opentok.key");
        String secret = configuration.getString("opentok.secret");
        openTok = new OpenTok(key, secret);
    }

    public void setRedisHelper(RedisHelper redisHelper) { this.redisHelper = redisHelper; }

    /**
     * Find all the SessionUsers participating in a session.
     * @param id The session ID.
     * @return A list of all the participating SessionUsers.
     */
    @Transactional
    public List<SessionUser> getSessionUsers(int id) {
        return sessionDao.findUsersSession(id);
    }


    /**
     * Find a session by its id as seen by the specified user.
     * @param id Target Session id.
     * @param uid User id performing the search.
     * @return The session, or null if not found or the user doesn't participate in it.
     */
    public Session getById(int id, int uid) {
        return sessionDao.findById(id, uid);
    }


    /**
     * Find a session by its id.
     * @param id Target session id.
     * @return The session, or null if not found.
     */
    public Session getById(int id) {
        return sessionDao.findById(id);
    }


    /**
     * Find SessionUsers of a user, filtering by session state.
     * @param state A string specifying the target states.
     * @param uid Target user id.
     * @return A list of SessionUsers matching the criteria.
     */
    @Transactional
     public List<SessionUser> getState(String state, int uid) {
        Set<Session.State> states;

        switch (state) {
            case "programmed": states = EnumSet.of(Session.State.PROGRAMMED); break;
            case "underway": states = EnumSet.of(Session.State.REQUESTED, Session.State.UNDERWAY); break;
            case "closed": states = EnumSet.of(Session.State.CLOSED, Session.State.FINISHED); break;
            case "all": states = EnumSet.of(Session.State.PROGRAMMED, Session.State.REQUESTED, Session.State.UNDERWAY,
                    Session.State.CLOSED, Session.State.FINISHED); break;
            default: return null;
        }

        return sessionDao.findByState(states, uid);
    }

    /**
     * Find all SessionUsers of a user.
     * @param uid Target user id.
     * @return A list of Sessionusers of the user.
     */
    @Transactional
    public List<SessionUser> getSessions(int uid) {
        return getState("all", uid);
    }

    /**
     * Return a list of session ids for the given user.
     * @param uid The target user ID
     * @return A list of the ids of the user's sessions.
     */
    @Transactional
    public List<Integer> getUserSessionIds(int uid) {
        return sessionDao.findIdsByUser(uid);
    }

    /**
     * Return the target user's SessionUsers for sessions in the given state, limited to MAX_RESULTS.
     * @param uid The target user's ID
     * @param state States we're interested in
     * @return List of the selected SessionUsers.
     */
    @Transactional
    public List<SessionUser> getUserSessionsByState(int uid, Set<Session.State> state) {
        return getUserSessionsByState(uid, state, MAX_RESULTS);
    }

    /**
     * Return the target user's SessionUsers for sessions in the given state, up to limit entries.
     * @param uid The target user's ID
     * @param state States we're interested in
     * @param limit Max number of results, or a negative number to make it unlimited
     * @return List of the selected SessionUsers.
     */
    @Transactional
    public List<SessionUser> getUserSessionsByState(int uid, Set<Session.State> state, int limit) {
        return sessionDao.findByState(state, uid, limit);
    }

    /**
     * Finds a user SessionUsers of a service type and in the specified states.
     * @param uid Target user.
     * @param states States to filter by.
     * @param serviceTypeId Service type id to filter by.
     * @return List of all the matching SessionUsers.
     */
    @Transactional
    public List<SessionUser> getUserSessionsByService(int uid, Set<Session.State> states, int serviceTypeId) {
        ServiceType serviceType = serviceTypeDao.findById(serviceTypeId);
        if (serviceType == null) return null;
        return sessionDao.findByService(uid, states, serviceTypeId);
    }

    /**
     * Creates a new requested session
     * @param uid           Target user id.
     * @param serviceType   Requested service type.
     * @param state         Initial state for the session.
     * @return Session      The newly-created session.
     */
    @Transactional
    public Session requestSession(int uid, int serviceType, Session.State state) {
        return requestSession(uid, serviceType, state, new Date());
    }


    /**
     * Creates a new programmed session
     * @param uid           Target user id.
     * @param serviceType   Requested service type.
     * @param state         Initial state for the session.
     * @param startDate     If state is PROGRAMMED, the chosen date for the session.
     * @return Session      The newly-created session.
     */
    @Transactional
    public Session requestSession(int uid, int serviceType, Session.State state, Date startDate) {
        ServiceType service = serviceTypeDao.findById(serviceType);
        if(service == null) return null;

        User user = userDao.findById(uid);
        if(user == null) return null;

        String title = user.getFullName();
        if (title.length() == 0) {
            title = conf.getString("entity.session") + " " + new SimpleDateFormat("dd-MM-yyyy").format(startDate);
        }

        // Update user balance
        int price = service.getPrice();
        if (user.getBalance() < price) return null;
        user.setBalance(user.getBalance() - price);

        // Create session
        Session session = new Session(service, title, startDate, state);
        SessionUser sessionUser = new SessionUser(user, session);

        // OpenTok integration
        if (service.getMode() == ServiceType.ServiceMode.VIDEO) {
            try {
                // Create room
                com.opentok.Session otSession = openTok.createSession();
                ObjectNode meta = Json.newObject();
                meta.put("opentokSession", otSession.getSessionId());
                session.setMeta(meta);

                // Create token for this user
                TokenOptions.Builder builder = new TokenOptions.Builder();
                builder.expireTime(System.currentTimeMillis() / 1000 + 3600*24*30);
                ObjectNode meta2 = Json.newObject();
                meta2.put("opentokToken", otSession.generateToken(builder.build()));
                sessionUser.setMeta(meta2);
            } catch (OpenTokException e) {
                // No video, no session! Break the transaction.
                throw new RuntimeException(e);
            }
        }

        sessionDao.create(session);
        sessionUserDao.create(sessionUser);

        // Add transaction
        Transaction transaction = new Transaction(user, session);
        transactionDao.create(transaction);

        Controller.sendMessage("session.users",
                Json.newObject()
                        .put("id", session.getId())
                        .putPOJO("userIds", Json.toJson(getSessionParticipants(session.getId())))
                        .toString());

        Controller.sendMessage("sessions.pools",
                Json.newObject()
                        .putPOJO("serviceTypes", Json.toJson(getPoolsSize()))
                        .toString());

        return session;
    }


    /**
     * Returns a map of the non-empty session pools, with their queue length.
     * @return A map keyed by the pool ids, with their queue length as the value.
     */
    @Transactional
    public Map<Integer, Integer> getPoolsSize() {
        return sessionDao.getPoolsSize();
    }


    /**
     * Assigns a session from the selected pool to a given user.
     * @param uid The User (probably a Professional) that will get assigned.
     * @param service_type_id Target pool of services to pick from.
     * @return Session with the User assigned to it.
     */
    @Transactional
    public Session assignSessionFromPool(int uid, int service_type_id) {
        User user = userDao.findById(uid);

        ServiceType type = serviceTypeDao.findById(service_type_id);

        // Can the User assign more Sessions?
        int current = sessionDao.countByStateAndType(uid, EnumSet.of(Session.State.UNDERWAY), service_type_id);
        if (type.getUserlimit() != null && current >= type.getUserlimit()) {
            String msg = "You have too many sessions";
            throw new InvalidStateException(msg, ServiceType.class, service_type_id);
        }

        Service service = serviceDao.getServiceForType(uid, service_type_id);
        Session session = sessionDao.getFromPool(service_type_id);
        if (session == null) {
            String msg = "No pending sessions found";
            throw new InvalidStateException(msg, ServiceType.class, service_type_id);
        }

        SessionUser sessionUser = new SessionUser(user, session);
        if (service != null) {
            sessionUser.setService(service);
        }
        // OpenTok integration
        if (session.getServiceType().getMode() == ServiceType.ServiceMode.VIDEO) {
            try {
                if (session.getMeta() == null || !session.getMeta().has("opentokSession")) {
                    throw new RuntimeException("Video session without OpenTok session assigned.");
                }
                // Create token for this user
                String otSession = session.getMeta().get("opentokSession").asText();
                TokenOptions.Builder builder = new TokenOptions.Builder();
                builder.expireTime(System.currentTimeMillis() / 1000 + 3600*24*30);
                ObjectNode meta = Json.newObject();
                meta.put("opentokToken", openTok.generateToken(otSession, builder.build()));
                sessionUser.setMeta(meta);
            } catch (OpenTokException e) {
                // No video, no session! Break the transaction.
                throw new RuntimeException(e);
            }
        }
        sessionUserDao.create(sessionUser);

        session.setState(Session.State.UNDERWAY);

        Controller.sendMessage("session.users",
                Json.newObject()
                        .put("id", session.getId())
                        .putPOJO("userIds", Json.toJson(getSessionParticipants(session.getId())))
                        .toString());

        Controller.sendMessage("sessions.pools",
                Json.newObject()
                        .putPOJO("serviceTypes", Json.toJson(getPoolsSize()))
                        .toString());

        return session;
    }

    /**
     * Append a message to a session.
     * @param sessionId Target session.
     * @param message Message to append.
     * @return A list of all the messages in the target session.
     */
    @Transactional
    public ArrayNode appendChatMessage(int sessionId, String message) {
        Jedis redis = redisHelper.getResource();
        String key = redisHelper.generateKey(sessionId);

        redis.rpush(key, message);

        return getChatMessages(sessionId);
    }

    /**
     * Get all messages in a session.
     * @param sessionId Target session.
     * @return List of all the messages in the target session.
     */
    @Transactional
    public ArrayNode getChatMessages(int sessionId) {
        Jedis redis = redisHelper.getResource();
        String key = redisHelper.generateKey(sessionId);
        int size = redis.llen(key).intValue();

        List<String> redisMessages = redis.lrange(key, 0, size);
        ArrayNode messages = new ArrayNode(JsonNodeFactory.instance);
        for (String redisMessage : redisMessages) {
            try {
                messages.add(Json.parse(redisMessage));
            } catch(RuntimeException e) {
                // prevent parse errors
            }
        }

        return messages;
    }


    /**
     * Close a session, if the user can close it.
     * If the session is not underway, no changes will be made.
     * @param sessionId Target session to close.
     * @param userId User performing the action.
     * @return The session if the user has access to it, or null otherwise.
     */
    @Transactional
    public Session close(int sessionId, int userId) {
        Session session = getById(sessionId, userId);
        if (session == null)
            throw new TargetNotFoundException(Session.class, sessionId);
        if (session.getState() != Session.State.UNDERWAY)
            throw new InvalidStateException("Session not underway", Session.class, sessionId);
        session.setState(Session.State.CLOSED);
        Controller.sendMessage("session.close", Json.toJson(session).toString());
        return session;
    }


    /**
     * Finish a session, if the user can finish it.
     * If the session is not closed, no changes will be made.
     * @param sessionId Target session to finish.
     * @param userId User performing the action.
     * @return The session if the user has access to it, or null otherwise.
     */
    @Transactional
    public Session finish(int sessionId, int userId) {
        /*
            To finish a session, it must met the following conditions first:
              - State must be CLOSED
              - All Client participants must have a report set
              - The user initiating the action must be on the session
              - There must be at least one paying user and one earning user
            This action will perform the following modifications:
              - The state will be set to FINISHED and its timestamp updated to NOW
              - All Professional participants with a Service set on their respective SessionUser
                will get their earnings added to their balance
              - An AMQP message will be sent detailing the changes
            NOTE: No balance will be deducted from the paying clients: they have payed already to start the session.
         */

        // Precondition checks
        Session session = getById(sessionId, userId);
        if (session == null)
            throw new TargetNotFoundException(Session.class, sessionId);
        if (session.getState() != Session.State.CLOSED)
            throw new InvalidStateException("Session not closed", Session.class, sessionId);
        List<SessionUser> sessionUsers = sessionUserDao.findBySession(sessionId);
        List<SessionUser> payers = new ArrayList<>();
        List<SessionUser> earners = new ArrayList<>();
        for (SessionUser sessionUser: sessionUsers) {
            User user = sessionUser.getUser();
            if (user.getUserType().equals("client")) {
                if (sessionUser.getReport() == null || sessionUser.getReport().length() == 0)
                    throw new InvalidStateException("Client without report", User.class, user.getId());
                payers.add(sessionUser);
            } else if (user.getUserType().equals("professional")) {
                if (sessionUser.getService() != null) earners.add(sessionUser);
            }
        }
        if (payers.size() < 1)
            throw new InvalidStateException("Session without any Clients", Session.class, sessionId);
        if (earners.size() < 1)
            throw new InvalidStateException("Session without any Professionals on Service", Session.class, sessionId);

        // Everything seems fine. Apply changes.
        for (SessionUser earner: earners) {
            Transaction transaction = new Transaction(earner.getUser(), session, earner.getService());
            transactionDao.create(transaction);
            // Update the balance
            int balance = earner.getUser().getBalance();
            balance += earner.getService().getEarnings();
            earner.getUser().setBalance(balance);
        }
        session.setState(Session.State.FINISHED);
        session.setTimestamp(new Date());


        Controller.sendMessage("session.finish",
                Json.newObject()
                        .putPOJO("sessionId", sessionId)
                        .putPOJO("clients", payers.stream().map(o -> o.getUser().getId()).collect(Collectors.toList()))
                        .putPOJO("professionals", payers.stream().map(o -> o.getUser().getId()).collect(Collectors.toList()))
                        .toString());

        return session;
    }


    /**
     * Sets or updates the report of a specified SessionUser.
     * If the session is not underway or closed, or the editor is not on the same session, no changes will be made.
     * @param sessionUserId Target sessionUser to update.
     * @param userId User performing the action.
     * @param report New value for the report field.
     * @return The sessionUser if the user has access to it, or null otherwise.
     */
    @Transactional
    public SessionUser setReport(int sessionUserId, int userId, String report) {
        SessionUser sessionUser = sessionUserDao.findById(sessionUserId);
        Session session = getById(sessionUser.getSession().getId(), userId);
        if (session == null)
            throw new TargetNotFoundException(SessionUser.class, sessionUserId);
        if (!EnumSet.of(Session.State.UNDERWAY, Session.State.CLOSED).contains(session.getState())) {
            throw new InvalidStateException("Session not closed or underway", Session.class, session.getId());
        }
        sessionUser.setReport(report);
        return sessionUser;
    }

    /**
     * Returns a map of all the currently open sessions to their participants.
     */
    public Map<Integer, List<Integer>> getCurrentSessionsParticipants() {
        return sessionDao.getSessionsParticipants(EnumSet.of(Session.State.REQUESTED, Session.State.UNDERWAY));
    }

    /**
     * Returns a list of participants of the specified session.
     */
    public List<Integer> getSessionParticipants(int sessionId) {
        return sessionDao.getSessionParticipants(sessionId);
    }

}

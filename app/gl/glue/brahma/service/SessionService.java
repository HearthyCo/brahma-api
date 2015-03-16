package gl.glue.brahma.service;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
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
import gl.glue.brahma.model.user.Client;
import gl.glue.brahma.model.user.Professional;
import gl.glue.brahma.model.user.User;
import gl.glue.brahma.model.user.UserDao;
import gl.glue.brahma.util.RedisHelper;
import play.db.jpa.Transactional;
import play.libs.Json;
import redis.clients.jedis.Jedis;

import java.text.SimpleDateFormat;
import java.util.*;

public class SessionService {

    private static Config conf = ConfigFactory.load();

    private static final int MAX_RESULTS = 2;
    private static final int MAX_SESSIONS_PER_PROFESSIONAL = 10;
    private UserDao userDao = new UserDao();
    private SessionDao sessionDao = new SessionDao();
    private SessionUserDao sessionUserDao = new SessionUserDao();
    private ServiceDao serviceDao = new ServiceDao();
    private ServiceTypeDao serviceTypeDao = new ServiceTypeDao();
    private TransactionDao transactionDao = new TransactionDao();
    private RedisHelper redisHelper = new RedisHelper();

    public void setRedisHelper(RedisHelper redisHelper) { this.redisHelper = redisHelper; }

    /**
     * Create Session JSON object from object array from Session DAO
     * @param id Object array to read session values
     * @return ObjectNode with a session with values passed in sessionObject.
     */
    @Transactional
    public List<SessionUser> getSessionUsers(int id) {
        return sessionDao.findUsersSession(id);
    }


    /**
     * Search session by id
     * @param id Session id to search
     * @param uid User login to search
     * @return Session with id passed
     */
    public Session getById(int id, int uid) {
        return sessionDao.findById(id, uid);
    }


    /**
     * Search session by id
     * @param id Session id to search
     * @return Session with id passed
     */
    public Session getById(int id) {
        return sessionDao.findById(id);
    }


    /**
     * Search sessions with state passed
     * @param state Session State to search
     * @param uid User Login to search
     * @return Object array list with sessions with state passed
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
     * Search sessions with state passed
     * @param state Session State to search
     * @param uid User Login to search
     * @return Object array list with sessions with state passed
     */
    @Transactional
    public List<SessionUser> getAllState(int uid) {
        return getState("all", uid);
    }

    /**
     * Return a list of session ids for the given user.
     * @param uid The target user ID
     * @return ObjectNode with all sessions grouped by state.
     */
    @Transactional
    public List<Integer> getUserSessionIds(int uid) {
        return sessionDao.findIdsByUser(uid);
    }

    /**
     * Return Sessions by user to show in home screen
     * @param uid User Login to search in DAO functions
     * @return ObjectNode with all sessions grouped by state.
     */
    @Transactional
    public List<SessionUser> getUserSessionsByState(int uid, Set<Session.State> state) {
        return sessionDao.findByState(state, uid, MAX_RESULTS);
    }

    /**
     * Return Sessions by user filtered to show in home screen
     * @param uid User Login to search in DAO functions
     * @return ObjectNode with all sessions grouped by state.
     */
    @Transactional
    public List<SessionUser> getUserSessionsByService(int uid, Set<Session.State> states, int serviceTypeId) {
        ServiceType serviceType = serviceTypeDao.findById(serviceTypeId);
        if (serviceType == null) return null;
        return sessionDao.findByService(uid, states, serviceTypeId);
    }

    /**
     * Creates a new requested session
     * @param uid           User Login to create session
     * @param serviceType   Service type id to search in DAO functions
     * @param state         Session state
     * @return Session      Session created
     */
    @Transactional
    public Session requestSession(int uid, int serviceType, Session.State state) {
        return requestSession(uid, serviceType, state, new Date());
    }


    /**
     * Creates a new programmed session
     * @param uid           User Login to create session
     * @param serviceType   Service type id to search in DAO functions
     * @param state         Session state
     * @param startDate     Date when start session, this param only is important if state is "PROGRAMMED"
     * @return Session      Session created
     */
    @Transactional
    public Session requestSession(int uid, int serviceType, Session.State state, Date startDate) {
        String title = conf.getString("entity.session") + " " + new SimpleDateFormat("dd-MM-yyyy").format(startDate);

        ServiceType service = serviceTypeDao.findById(serviceType);
        if(service == null) return null;

        User user = userDao.findById(uid);
        if(user == null) return null;

        // Update user balance
        int price = service.getPrice();
        if (user.getBalance() < price) return null;
        user.setBalance(user.getBalance() - price);

        // Add transaction
        String reason = "Payment session " + title;
        Transaction transaction = new Transaction(user, price * -1, Transaction.State.APPROVED, null, reason);
        transactionDao.create(transaction);

        Session session = new Session(service, title, startDate, state);
        sessionDao.create(session);

        SessionUser sessionUser = new SessionUser(user, session);
        sessionUserDao.create(sessionUser);

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

        // Can the User assign more Sessions?
        int current = sessionDao.countByState(EnumSet.of(Session.State.UNDERWAY), uid);
        if (current >= MAX_SESSIONS_PER_PROFESSIONAL) return null;

        Service service = serviceDao.getServiceForType(uid, service_type_id);
        Session session = sessionDao.getFromPool(service_type_id);
        if (session == null) return null;

        SessionUser sessionUser = new SessionUser();
        sessionUser.setUser(user);
        sessionUser.setSession(session);
        sessionUser.setMeta(Json.newObject());
        if (service != null) {
            sessionUser.setService(service);
        }
        sessionUserDao.create(sessionUser);

        session.setState(Session.State.UNDERWAY);
        return session;
    }

    /**
     * Append a message in session
     * @param sessionId Target session to append message.
     * @param message Message for append
     * @return List of all messages for target session
     */
    @Transactional
    public ArrayNode appendChatMessage(int sessionId, String message) {
        Jedis redis = redisHelper.getResource();
        String key = redisHelper.generateKey(sessionId);

        redis.rpush(key, message);

        return getChatMessages(sessionId);
    }

    /**
     * Get all messages in session
     * @param sessionId Target session to append message.
     * @return List of all messages for target session
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

}

package gl.glue.brahma.service;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gl.glue.brahma.model.session.Session;
import gl.glue.brahma.model.session.SessionDao;
import gl.glue.brahma.model.sessionuser.SessionUser;
import gl.glue.brahma.model.sessionuser.SessionUserDao;
import gl.glue.brahma.model.user.Client;
import gl.glue.brahma.model.user.Professional;
import gl.glue.brahma.model.user.User;
import play.db.jpa.Transactional;
import play.libs.Json;

import java.util.*;

public class SessionService {

    private SessionDao sessionDao = new SessionDao();
    private SessionUserDao sessionUserDao = new SessionUserDao();

    /**
     * Create Session JSON object from object array from Session DAO
     * @param id Object array to read session values
     * @param uid Object array to read session values
     * @return ObjectNode with a session with values passed in sessionObject.
     */
    @Transactional
    public ObjectNode getSession(int id, int uid) {
        // Find session
        Session session = sessionDao.findById(id, uid);
        if (session == null) return null;

        ObjectNode users = Json.newObject();

        // Find users for session
        User me = null;
        List<SessionUser> sessionUsers = sessionDao.findUsersSession(id);

        ArrayNode clients = new ArrayNode(JsonNodeFactory.instance);
        ArrayNode profesionals = new ArrayNode(JsonNodeFactory.instance);

        for (SessionUser sessionUser : sessionUsers) {
            ObjectNode user = (ObjectNode) Json.toJson(sessionUser.getUser());

            user.put("sessionMeta", sessionUser.getMeta());

            if(sessionUser.getUser() instanceof Client) {
                user.put("report", sessionUser.getReport());
            }

            if (sessionUser.getUser() instanceof Professional) {
                if(sessionUser.getService() != null) {
                    user.put("service", sessionUser.getService().getServiceType().getField().getName());
                }
            }

            if (uid == sessionUser.getUser().getId()) {
                users.put("me", user);

                sessionUser.setViewedDate(new Date());
                sessionUserDao.save(sessionUser);
            }
            else if (sessionUser.getUser() instanceof Client) {
                clients.add(user);
            }
            else if (sessionUser.getUser() instanceof Professional) {
                profesionals.add(user);
            }


        }

        // Add objects to result depends on userType
        users.put("professionals", profesionals);

        if (me instanceof Professional) {
            users.put("clients", clients);
        }

        ObjectNode sessionNode = (ObjectNode) Json.toJson(session);
        sessionNode.put("users", users);

        ObjectNode result = Json.newObject();
        result.put("session", sessionNode);

        return result;
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
            case "underway": states = EnumSet.of(Session.State.UNDERWAY); break;
            case "closed": states = EnumSet.of(Session.State.CLOSED, Session.State.FINISHED); break;
            default: return null;
        }

        return sessionDao.findByState(states, uid);
    }
}

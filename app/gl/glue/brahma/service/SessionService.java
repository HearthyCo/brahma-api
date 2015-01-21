package gl.glue.brahma.service;

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
        Session sessionFromDao = sessionDao.findById(id, uid);
        if (sessionFromDao == null) return null;

        ObjectNode session = (ObjectNode) Json.toJson(sessionFromDao);

        ObjectNode users = Json.newObject();

        // Find users for session
        User me = null;
        List<SessionUser> sessionUsers = sessionDao.findUsersSession(id);

        List<SessionUser> clients = new ArrayList<>();
        List<SessionUser> profesionals = new ArrayList<>();

        for (SessionUser sessionUser : sessionUsers) {
            if (uid == sessionUser.getUser().getId()) {
                me = sessionUser.getUser();
                users.put("me", Json.toJson(sessionUser/*, alowed fields */));

                sessionUser.setViewedDate(new Date());
                sessionUserDao.save(sessionUser);

                sessionUsers.remove(sessionUser);
            }
            else {
                if(sessionUser.getUser() instanceof Client) {
                    clients.add(sessionUser);
                }
                else if (sessionUser.getUser() instanceof Professional) {
                    profesionals.add(sessionUser);
                }
            }
        }

        // Add objects to result depends on userType
        if (me instanceof Client) {
            users.put("professionals", Json.toJson(profesionals));
        }
        else if (me instanceof Professional) {
            users.put("clients", Json.toJson(clients));
            users.put("professionals", Json.toJson(profesionals));
        }

        session.put("users", Json.toJson(users));

        ObjectNode result = Json.newObject();
        result.put("session", session);

        return result;
    }

    /**
     * Search sessions with state passed
     * @param state Session State to search
     * @param uid User Login to search
     * @return Object array list with sessions with state passed
     */
    @Transactional
    public List<ObjectNode> getState(String state, int uid) {
        Set<Session.State> states;

        switch (state) {
            case "programmed": states = EnumSet.of(Session.State.PROGRAMMED); break;
            case "underway": states = EnumSet.of(Session.State.UNDERWAY); break;
            case "closed": states = EnumSet.of(Session.State.CLOSED, Session.State.FINISHED); break;
            default: return null;
        }

        List<SessionUser> sessionUsers = sessionDao.findByState(states, uid);
        List<ObjectNode> result = new ArrayList<>();

        for (SessionUser sessionUser : sessionUsers) {
            result.add((ObjectNode) Json.toJson(sessionUser));
        }

        return result;
    }
}

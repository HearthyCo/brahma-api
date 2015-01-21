package gl.glue.brahma.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import gl.glue.brahma.model.session.Session;
import gl.glue.brahma.model.session.SessionDao;
import gl.glue.brahma.model.session.SessionUtils;
import gl.glue.brahma.model.user.UserUtils;
import play.db.jpa.Transactional;
import play.libs.Json;

import java.util.ArrayList;
import java.util.List;

public class SessionService {
    private SessionDao sessionDao = new SessionDao();

    /**
     * Clear user class string for return user type
     * @param type Class string
     * @return User type (client, professional, ...)
     */
    private String getUserClass(String type) {
        return type.split(" ")[1].split("\\.")[5].toLowerCase();
    }

    /**
     * Create ArrayList with users by type (client, professionals)
     * @param users Object array with mixed user types
     * @param type Type of user to group
     * @return Object array with user grouped by type.
     */
    private ArrayList<Object> getUsersByType(List<Object[]> users, String type) {
        ArrayList<Object> result = new ArrayList<Object>() {};

        for (Object[] userDao : users) {
            String userClass = userDao[7].toString();

            // Get type user
            String typeUSer = getUserClass(userClass);
            ObjectNode user = UserUtils.createUserObject(userDao);

            if (type.equals(typeUSer)) {
                result.add(user);
            }
        }

        return result;
    }

    /**
     * Create Session JSON object from object array from Session DAO
     * @param id Object array to read session values
     * @param login Object array to read session values
     * @return ObjectNode with a session with values passed in sessionObject.
     */
    @Transactional
    public ObjectNode getSession(int id, String login) {
        ObjectNode result = Json.newObject();

        // Find session
        Session sessionFromDao = sessionDao.findById(id, login);
        result.put("session", Json.toJson(sessionFromDao));

        ObjectNode users = Json.newObject();
        if (sessionFromDao != null) {
            String userType = "";

            // Find users for session
            List<Object[]> usersDao = sessionDao.findUsersSession(id);

            if(usersDao != null) {
                for (Object[] user : usersDao) {
                    if (login.equals(user[1].toString())) {
                        userType = getUserClass(user[7].toString());

                        users.put("me", UserUtils.createUserObject(user));
                        usersDao.remove(user);

                        int resultSetViewedDate = sessionDao.setSessionUserViewedDate(id);
                        result.put("updated", (resultSetViewedDate >= 1) ? true : false);
                    }
                }

                ArrayList<Object> clients = getUsersByType(usersDao, "client");
                ArrayList<Object> profesionals = getUsersByType(usersDao, "professional");

                // Add objects to result depends on userType
                if (userType.equals("client")) {
                    users.put("professionals", Json.toJson(profesionals));
                }
                else if (userType.equals("professional")) {
                    users.put("clients", Json.toJson(clients));
                    users.put("professionals", Json.toJson(profesionals));
                }

                // Add users in session object to result
                result.put("users", Json.toJson(users));
            }
            return result;
        } else {
            return null;
        }
    }

    /**
     * Search sessions with state passed
     * @param state Session State to search
     * @param login User Login to search
     * @return Object array list with sessions with state passed
     */
    @Transactional
    public ArrayList<ObjectNode> getState(String state, String login) {
        List<Session.State> states;

        switch (state) {
            case "programmed": states = new ArrayList<Session.State>(){{ add(Session.State.PROGRAMMED); }};
                break;
            case "underway": states = new ArrayList<Session.State>(){{ add(Session.State.UNDERWAY); }};
                break;
            case "closed": states = new ArrayList<Session.State>(){{ add(Session.State.CLOSED); add(Session.State.FINISHED); }};
                break;
            default: return null;
        }

        List<Object[]> sessionsDao = sessionDao.findByState(states, login);
        ArrayList<ObjectNode> result = new ArrayList<ObjectNode>() {};

        if (sessionsDao != null) {
            for (Object[] sessionDao : sessionsDao) {
                result.add(SessionUtils.createSessionObject(sessionDao));
            }

            return result;
        }
        else {
            return null;
        }
    }
}

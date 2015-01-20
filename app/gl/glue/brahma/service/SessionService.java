package gl.glue.brahma.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import gl.glue.brahma.model.session.Session;
import gl.glue.brahma.model.session.SessionDao;
import play.db.jpa.Transactional;
import play.libs.Json;

import java.util.ArrayList;
import java.util.List;

public class SessionService {
    private SessionDao sessionDao = new SessionDao();

    private String getUserClass(String type) {
        return type.split(" ")[1].split("\\.")[5];
    }

    private ObjectNode createUserObject(Object[] userObject) {
        ObjectNode user = Json.newObject();

        user.put("login", userObject[0].toString());
        user.put("name", userObject[1].toString());
        if(userObject[2] != null) user.put("surname1", userObject[2].toString());
        if(userObject[3] != null) user.put("surname2", userObject[3].toString());
        if(userObject[4] != null) user.put("avatar", userObject[4].toString());
        if(userObject[5] != null) user.put("service", userObject[5].toString());
        if(userObject[7] != null) user.put("report", userObject[7].toString());

        return user;
    }

    @Transactional
    public ObjectNode getSession(int id, String login) {

        ObjectNode result = Json.newObject();

        // Find session
        Session session = sessionDao.findById(id, login);
        if (session != null) {
            // Add session object to result
            result.put("session", Json.toJson(session));

            String userType = "";
            Boolean readed = false;
            ArrayList<Object> clients = new ArrayList<Object>() {};
            ArrayList<Object> profesionals = new ArrayList<Object>() {};

            // Find users for session
            List<Object[]> usersDao = sessionDao.findUsersSession(id);
            if (usersDao != null) {
                for(Object[] userDao : usersDao) {
                    String loginDao = userDao[0].toString();
                    String userClass = userDao[6].toString();

                    // Get type user
                    String type = getUserClass(userClass);
                    ObjectNode user = createUserObject(userDao);

                    // Id user is equal to login save type login user in userType and add object "me" to result
                    if (loginDao.equals(login)) {
                        // Readed SessionUser
                        readed = true;
                        userType = type;
                        result.put("me", user);
                    }
                    else {
                        switch (type) {
                            case "Client":
                                clients.add(user);
                                break;
                            case "Professional":
                                profesionals.add(user);
                                break;
                            default:
                                break;
                        }
                    }
                }

                // Add objects to result depends on userType
                if(userType.equals("Client")) {
                    result.put("professionals", Json.toJson(profesionals));
                }
                else if(userType.equals("Professional")) {
                    result.put("clients", Json.toJson(profesionals));
                    result.put("professionals", Json.toJson(profesionals));
                }

                if(readed) {
                    int resultSetViewedDate = sessionDao.setSessionUserViewedDate(id);
                    result.put("updated", (resultSetViewedDate == 1) ? true : false);
                }
            }
            return result;
        } else {
            return null;
        }
    }

    @Transactional
    public List<Object[]> getState(String state, String login) {
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

        List<Object[]> session = sessionDao.findByState(states, login);
        if (session != null) {
            return session;
        }
        else {
            return null;
        }
    }
}

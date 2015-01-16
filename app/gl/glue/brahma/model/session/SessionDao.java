package gl.glue.brahma.model.session;

import gl.glue.brahma.model.sessionuser.SessionUser;
import gl.glue.brahma.model.user.User;
import play.db.jpa.JPA;

import javax.persistence.NoResultException;
import java.util.List;

public class SessionDao {

    public Session findById(int id, String login) {
        try {
            String query =
                "select session from Session session, SessionUser sessionUser" +
                " where session.id = :id and sessionUser.session.id = session.id and sessionUser.user.login = :login";

            return JPA.em().createQuery(query, Session.class)
                    .setParameter("id", id)
                    .setParameter("login", login)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<Session> findByState(List<Session.State> states, String login) {
        try {
            String query =
                "select session from Session session, SessionUser sessionUser " +
                "where session.state in :states " +
                "and sessionUser.session.id = session.id and sessionUser.user.login = :login";

            return JPA.em().createQuery(query, Session.class)
                    .setParameter("states", states)
                    .setParameter("login", login)
                    .getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<User> findUsers(int id) {
        try {
            String query = "select user from User user " +
                "right join SessionUser sessionUser on (user.id = sessionUser.user) " +
                "left join Service service on (sessionUser.service = service.id) " +
                "left join ServiceType serviceType on (service.serviceType = serviceType.id) " +
                "left join Field field on (serviceType.field = field.id) " +
                "where sessionUser.id = :id";

            return JPA.em().createQuery(query, User.class)
                    .setParameter("id", id)
                    .getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }
}

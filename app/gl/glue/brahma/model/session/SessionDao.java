package gl.glue.brahma.model.session;

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

    public List<Object[]> findUsersSession(int id) {
        try {
            String query = "select user.login, user.name, user.surname1, user.surname2, user.avatar, field.name, type(user) " +
                    "from SessionUser sessionUser " +
                    "left join sessionUser.user user " +
                    "left join sessionUser.service service " +
                    "left join service.serviceType serviceType " +
                    "left join serviceType.field field " +
                    "where sessionUser.session.id = :id";

            return JPA.em().createQuery(query)
                    .setParameter("id", id)
                    .getResultList();

        } catch (NoResultException e) {
            return null;
        }
    }
}

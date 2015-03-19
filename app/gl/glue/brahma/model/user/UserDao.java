package gl.glue.brahma.model.user;

import gl.glue.brahma.model.sessionuser.SessionUser;
import play.db.jpa.JPA;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.List;

public class UserDao {

    public void create(User user) {
        JPA.em().persist(user);
    }

    @Deprecated
    public User findByLogin(String login) {
        try {
            return JPA.em().createNamedQuery("User.findByLogin", User.class)
                    .setParameter("login", login)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public User findByEmail(String email) {
        email = email.toLowerCase();
        try {
            return JPA.em().createNamedQuery("User.findByEmail", User.class)
                    .setParameter("email", email)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public User findById(int id) {
        return JPA.em().find(User.class, id);
    }

    public <T extends User> List<T> findByType(Class<T> userTypeClass) {
        String query = "User.findByTypeUser";
        if (Admin.class.isAssignableFrom(userTypeClass)) query = "User.findAdmins";
        if (Client.class.isAssignableFrom(userTypeClass)) query = "User.findClients";
        if (Coordinator.class.isAssignableFrom(userTypeClass)) query = "User.findCoordinators";
        if (Professional.class.isAssignableFrom(userTypeClass)) query = "User.findProfessionals";
        if (Tutor.class.isAssignableFrom(userTypeClass)) query = "User.findTutors";

        Query queryListUserType = JPA.em().createNamedQuery(query, userTypeClass);

        return queryListUserType.getResultList();
    }
}

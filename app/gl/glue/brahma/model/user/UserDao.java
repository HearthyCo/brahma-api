package gl.glue.brahma.model.user;

import play.db.jpa.JPA;

import javax.persistence.NoResultException;

public class UserDao {

    public void create(User user) {
        JPA.em().persist(user);
    }

    @Deprecated
    public User findByLogin(String login) {
        try {
            return JPA.em().createQuery("select x from User x where x.login = :login", User.class)
                    .setParameter("login", login)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public User findByEmail(String email) {
        email = email.toLowerCase();
        try {
            return JPA.em().createQuery("select x from User x where x.email = :email", User.class)
                    .setParameter("email", email)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public User findById(int id) {
        try {
            return JPA.em().createQuery("select x from User x where x.id = :id", User.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

}

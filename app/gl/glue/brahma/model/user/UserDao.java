package gl.glue.brahma.model.user;

import play.db.jpa.JPA;

import javax.persistence.NoResultException;

public class UserDao {

    public void save(User user) {
        JPA.em().persist(user);
        JPA.em().flush();
    }

    public User findByLogin(String login) {
        try {
            return JPA.em().createQuery("select x from User x where x.login = :login", User.class)
                    .setParameter("login", login)
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

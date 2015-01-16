package gl.glue.brahma.service;

import gl.glue.brahma.model.user.User;
import gl.glue.brahma.model.user.UserDao;
import play.db.jpa.Transactional;

public class UserService {

    private UserDao userDao = new UserDao();

    @Transactional
    public User login(String username, String password) {
        User user = userDao.findByLogin(username);
        if (user != null && user.canLogin() && user.authenticate(password)) {
            return user;
        } else {
            return null;
        }
    }

    @Transactional
    public User register(User user) {
        userDao.save(user);
        return user;
    }

    @Transactional
    public User get(int id) {
        User user = userDao.findById(id);
        if (user != null) {
            return user;
        } else {
            return null;
        }
    }

}

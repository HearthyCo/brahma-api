package gl.glue.brahma.service;

import gl.glue.brahma.model.user.User;
import gl.glue.brahma.model.user.UserDao;
import gl.glue.brahma.util.Mailer;
import org.apache.commons.lang3.RandomStringUtils;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.libs.Json;

public class UserService {

    private UserDao userDao = new UserDao();

    @Transactional
    public User login(String email, String password) {
        User user = userDao.findByEmail(email);
        if (user != null && user.canLogin() && user.authenticate(password)) {
            return user;
        } else {
            return null;
        }
    }

    @Transactional
    public User register(User user) {
        user.setEmail(user.getEmail().toLowerCase());
        // Email confirmation
        user.setConfirmed(false);
        user.mergeMeta(Json.newObject()
                .putPOJO("confirm", Json.newObject()
                        .putPOJO("mail", Json.newObject()
                                .put("hash", RandomStringUtils.randomAlphanumeric(32))
                                .put("expires", System.currentTimeMillis() + 86400000))));
        userDao.create(user);
        JPA.em().flush(); // Detect errors right now, before sending junk mail.
        Mailer.send(user, Mailer.MailTemplate.REGISTER_CONFIRM_MAIL);
        return user;
    }

    @Transactional
    public User getById(int uid) {
        User user = userDao.findById(uid);
        if (user != null) {
            return user;
        } else {
            return null;
        }
    }
}

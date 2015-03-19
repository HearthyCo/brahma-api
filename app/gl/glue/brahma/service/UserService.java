package gl.glue.brahma.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gl.glue.brahma.model.user.User;
import gl.glue.brahma.model.user.UserDao;
import gl.glue.brahma.util.Notificator;
import org.apache.commons.lang3.RandomStringUtils;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.libs.Json;

import java.util.List;

public class UserService {

    private UserDao userDao = new UserDao();

    @Transactional
    public User login(String email, String password) {
        User user = userDao.findByEmail(email);
        if (user != null && user.authenticate(password)) {
            return user;
        } else {
            return null;
        }
    }

    @Transactional
    public User register(User user) {
        user.setEmail(user.getEmail().toLowerCase());
        // Email confirmation
        user.mergeMeta(Json.newObject()
                .putPOJO("confirm", Json.newObject()
                        .putPOJO("mail", Json.newObject()
                                .put("hash", RandomStringUtils.randomAlphanumeric(32))
                                .put("expires", System.currentTimeMillis() + 86400000))));
        userDao.create(user);
        JPA.em().flush(); // Detect errors right now, before sending junk mail.
        Notificator.send(user, Notificator.NotificationEvents.USER_REGISTER);
        return user;
    }

    @Transactional
    public User getById(int uid) {
        return userDao.findById(uid);
    }

    @Transactional
    public <T extends User> List<T> getByType(Class<T> userTypeClass) {
        return userDao.findByType(userTypeClass);
    }

    @Transactional
    public boolean confirmMail(int uid, String hash) {
        User user = userDao.findById(uid);
        JsonNode mailConfirm = user.getMeta().path("confirm").path("mail");
        if (user.isConfirmed() || mailConfirm.isMissingNode()) return false;
        if (System.currentTimeMillis() > mailConfirm.get("expires").asLong()) return false;
        if (!mailConfirm.get("hash").asText().equals(hash)) return false;
        user.setState(User.State.CONFIRMED);
        // If adding another welcome mail, send it from here, like this:
        // Notificator.send(user, REGISTER_COMPLETE_MAIL);
        return true;
    }

    @Transactional
    public User requestPasswordChange(String email) {
        User user = userDao.findByEmail(email);
        if (user == null) return null;
        user.mergeMeta(Json.newObject()
                .putPOJO("confirm", Json.newObject()
                        .putPOJO("password", Json.newObject()
                                .put("hash", RandomStringUtils.randomAlphanumeric(32))
                                .put("expires", System.currentTimeMillis() + 86400000))));
        Notificator.send(user, Notificator.NotificationEvents.USER_RECOVER);
        return user;
    }

    @Transactional
    public boolean confirmPasswordChange(int uid, String hash, String newPassword) {
        User user = userDao.findById(uid);
        JsonNode meta = user.getMeta();
        JsonNode passConfirm = meta.path("confirm").path("password");
        if (passConfirm.isMissingNode()) return false;
        if (System.currentTimeMillis() > passConfirm.get("expires").asLong()) return false;
        if (!passConfirm.get("hash").asText().equals(hash)) return false;
        ((ObjectNode)meta.get("confirm")).remove("password");
        user.setMeta(meta);
        user.setPassword(newPassword);
        return true;
    }

}

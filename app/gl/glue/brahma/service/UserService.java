package gl.glue.brahma.service;

import com.amazonaws.services.s3.model.PutObjectRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gl.glue.brahma.model.user.User;
import gl.glue.brahma.model.user.UserDao;
import gl.glue.brahma.util.Notificator;
import org.apache.commons.lang3.RandomStringUtils;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.libs.Json;
import plugins.S3Plugin;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        Notificator.send(user, Notificator.NotificationEvents.USER_REGISTER,
                Json.newObject().put("link", "http://localhost:3000/irparaconfirmar"));
        return user;
    }

    @Transactional
    public User getById(int uid) {
        return userDao.findById(uid);
    }

    @Transactional
    public User getByEmail(String email) {
        return userDao.findByEmail(email);
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
        ((ObjectNode) meta.get("confirm")).remove("password");
        user.setMeta(meta);
        user.setPassword(newPassword);
        return true;
    }

    @Transactional
    public User setAvatar(int uid, File file) {
        User user = userDao.findById(uid);

        // If the user has one already, invalidate it!
        if (user.getAvatar() != null) {
            String oldKey = S3Plugin.url2key(user.getAvatar());
            if (oldKey != null) S3Plugin.removeFile(oldKey);
        }

        // Generate a random-looking key using a SHA-256 hash and base64encoding it
        String in = String.format("%d/%d", uid, System.currentTimeMillis());
        String key;
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            sha.update(in.getBytes());
            byte[] digest = sha.digest();
            String hash = Base64.getUrlEncoder().encodeToString(digest);
            key = "avatars/" + hash;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        // Add some metadata we'd like to keep (privately) with the file
        Map<String, String> userMetadata = new HashMap<>();
        userMetadata.put("userId", Integer.toString(uid));

        // Upload
        S3Plugin.putFile(key, file, userMetadata);
        String url = S3Plugin.key2url(key);

        user.setAvatar(url);
        return user;
    }
}

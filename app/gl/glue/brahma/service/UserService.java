package gl.glue.brahma.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import gl.glue.brahma.model.user.User;
import gl.glue.brahma.model.user.UserDao;
import gl.glue.brahma.plugins.StoragePlugin;
import gl.glue.brahma.util.Notificator;
import org.apache.commons.lang3.RandomStringUtils;
import play.Play;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.libs.Json;
import gl.glue.brahma.plugins.S3Plugin;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserService {

    private UserDao userDao = new UserDao();
    private static Config conf = null;
    private StoragePlugin storagePlugin = Play.application().plugin(S3Plugin.class);

    static {
        conf = ConfigFactory.load();
    }

    public void setStoragePlugin(StoragePlugin storagePlugin) {
        this.storagePlugin = storagePlugin;
    }

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
        String hash = RandomStringUtils.randomAlphanumeric(32);

        user.mergeMeta(Json.newObject()
                .putPOJO("confirm", Json.newObject()
                        .putPOJO("mail", Json.newObject()
                                .put("hash", hash)
                                .put("expires", System.currentTimeMillis() + 86400000))));

        userDao.create(user);
        JPA.em().flush(); // Detect errors right now, before sending junk mail.

        String confirmLink = conf.getString(user.getUserType() + ".uri") + "/confirm/mail/" + user.getId() + "/" + hash;
        Notificator.send(user, Notificator.NotificationEvents.USER_REGISTER, Json.newObject().put("link", confirmLink));
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
        Notificator.send(user, Notificator.NotificationEvents.USER_CONFIRM);
        return true;
    }

    @Transactional
    public User requestPasswordChange(String email) {
        User user = userDao.findByEmail(email);
        String hash = RandomStringUtils.randomAlphanumeric(32);

        if (user == null) return null;
        user.mergeMeta(Json.newObject()
                .putPOJO("confirm", Json.newObject()
                        .putPOJO("password", Json.newObject()
                                .put("hash", RandomStringUtils.randomAlphanumeric(32))
                                .put("expires", System.currentTimeMillis() + 86400000))));
        String confirmLink = conf.getString(user.getUserType() + ".uri") + "/confirm/mail/" + user.getId() + "/" + hash;
        Notificator.send(user, Notificator.NotificationEvents.USER_RECOVER_PASSWORD, Json.newObject().put("link", confirmLink));
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
        Notificator.send(user, Notificator.NotificationEvents.USER_CONFIRM_PASSWORD);
        return true;
    }

    @Transactional
    public User setAvatar(int uid, File file) {
        User user = userDao.findById(uid);

        // If the user has one already, invalidate it!
        if (user.getAvatar() != null) {
            String oldKey = storagePlugin.url2key(user.getAvatar());
            if (oldKey != null) storagePlugin.removeFile(oldKey);
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
        storagePlugin.putFile(key, file, userMetadata);
        String url = storagePlugin.key2url(key);

        user.setAvatar(url);
        return user;
    }
}

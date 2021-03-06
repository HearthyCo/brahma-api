package gl.glue.brahma.service;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import gl.glue.brahma.model.attachment.Attachment;
import gl.glue.brahma.model.attachment.AttachmentDao;
import gl.glue.brahma.model.session.Session;
import gl.glue.brahma.model.session.SessionDao;
import gl.glue.brahma.model.user.User;
import gl.glue.brahma.model.user.UserDao;
import gl.glue.brahma.plugins.StoragePlugin;
import gl.glue.play.amqp.Controller;
import play.Play;
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

public class AttachmentService {

    private AttachmentDao attachmentDao= new AttachmentDao();
    private SessionDao sessionDao = new SessionDao();
    private UserDao userDao = new UserDao();
    private StoragePlugin storagePlugin = Play.application().plugin(S3Plugin.class);

    public void setStoragePlugin(StoragePlugin storagePlugin) {
        this.storagePlugin = storagePlugin;
    }

    @Transactional
    public Attachment getById(int id) {
        return attachmentDao.getById(id);
    }

    @Transactional
    public List<Attachment> getBySession(int sid) {
        return attachmentDao.getBySession(sid);
    }

    @Transactional
    public Attachment uploadToSession(int userId, int sessionId, String filename, File file) {
        return uploadToSession(userId, sessionId, filename, file, null);
    }

    @Transactional
    public Attachment uploadToSession(int userId, int sessionId, String filename, File file, File thumb) {
        Session session = sessionDao.findById(sessionId, userId);
        User user = userDao.findById(userId);
        if (session == null) {
            return null;
        }

        // Generate a random-looking key using a SHA-256 hash and base64encoding it
        String in = String.format("%d/%d/%d", userId, sessionId, System.currentTimeMillis());
        String key;
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            sha.update(in.getBytes());
            byte[] digest = sha.digest();
            String hash = Base64.getUrlEncoder().encodeToString(digest);
            key = hash.substring(0, 2) + '/' + hash.substring(2,4) + '/' + hash.substring(4);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        // Add some metadata we'd like to keep (privately) with the file
        Map<String, String> userMetadata = new HashMap<>();
        userMetadata.put("userId", Integer.toString(userId));
        userMetadata.put("sessionId", Integer.toString(sessionId));

        // Upload
        String mime = storagePlugin.putFile(key, file, userMetadata);
        if (thumb != null) {
            storagePlugin.putFile(key + "_thumb", thumb, userMetadata);
        }

        // Finally add to DB
        Attachment attachment = new Attachment();
        attachment.setUser(user);
        attachment.setSession(session);
        attachment.setUrl(storagePlugin.key2url(key));
        attachment.setFilename(filename);
        attachment.setSize((int) file.length());
        attachment.setMime(mime);
        attachment.setHasThumb(thumb != null);
        attachmentDao.create(attachment);

        // And send AMQP notification
        Controller.sendMessage("chat.attachment",
            new ArrayNode(JsonNodeFactory.instance)
                .addPOJO(Json.newObject()
                    .put("id", "play.attachment." + attachment.getId())
                    .put("type", "attachment")
                    .put("session", sessionId)
                    .put("author", userId)
                    .putPOJO("data", Json.newObject()
                        .put("message", attachment.getFilename())
                        .put("href", attachment.getUrl())
                        .put("type", attachment.getMime())
                        .put("size", attachment.getSize())
                        .put("hasThumb", attachment.hasThumb())
                    )
                )
            .toString());

        return attachment;
    }

}

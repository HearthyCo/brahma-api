package gl.glue.brahma.service;

import gl.glue.brahma.model.attachment.Attachment;
import gl.glue.brahma.model.attachment.AttachmentDao;
import gl.glue.brahma.model.session.Session;
import gl.glue.brahma.model.session.SessionDao;
import gl.glue.brahma.model.user.User;
import gl.glue.brahma.model.user.UserDao;
import play.db.jpa.Transactional;
import plugins.S3Plugin;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class AttachmentService {

    private AttachmentDao attachmentDao= new AttachmentDao();
    private SessionDao sessionDao = new SessionDao();
    private UserDao userDao = new UserDao();

    @Transactional
    public Attachment getById(int id) {
        return attachmentDao.getById(id);
    }

    @Transactional
    public void create(Attachment attachment) {
        attachmentDao.create(attachment);
    }

    @Transactional
    public Attachment uploadToSession(int userId, int sessionId, String filename, File file) {
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
        S3Plugin.putFile(key, file, userMetadata);

        // Finally add to DB
        Attachment attachment = new Attachment();
        attachment.setUser(user);
        attachment.setSession(session);
        attachment.setUrl(S3Plugin.key2url(key));
        attachment.setFilename(filename);
        attachment.setSize((int) file.length());
        attachmentDao.create(attachment);

        return attachment;
    }

}
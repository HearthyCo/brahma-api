package gl.glue.brahma.test.service;

import gl.glue.brahma.model.attachment.Attachment;
import gl.glue.brahma.service.AttachmentService;
import gl.glue.brahma.service.UserService;
import org.junit.BeforeClass;
import org.junit.Test;
import utils.FakeStoragePlugin;
import utils.TransactionalTest;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AttachmentServiceTest extends TransactionalTest {

    private UserService userService = new UserService();
    private static AttachmentService attachmentService = new AttachmentService();

    @BeforeClass
    public static void setup() {
        attachmentService.setStoragePlugin(new FakeStoragePlugin());
    }

    @Test
    public void getAttachmentByIdOk() {
        Attachment attachment = attachmentService.getById(91201);
        assertNotNull(attachment);
        assertEquals("leaf.jpg", attachment.getFilename());
    }

    @Test
    public void getAttachmentBySessionOk() {
        List<Attachment> attachments = attachmentService.getBySession(90700);
        assertNotNull(attachments);
        assertEquals(1, attachments.size());
        assertEquals("leaf.jpg", attachments.get(0).getFilename());
    }

    @Test
    public void uploadToSessionOk() throws IOException {
        int uid = 90000;
        int sid = 90712;
        File file = File.createTempFile("test-file", ".jpg");
        file.deleteOnExit();
        Attachment attachment = attachmentService.uploadToSession(uid, sid, file.getName(), file);
        assertNotNull(attachment);
        assertEquals(file.getName(), attachment.getFilename());
        assertEquals(uid, attachment.getUser().getId());
        assertEquals(sid, attachment.getSession().getId());
    }

}
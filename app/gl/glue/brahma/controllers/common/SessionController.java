package gl.glue.brahma.controllers.common;

import actions.BasicAuth;
import gl.glue.brahma.model.attachment.Attachment;
import gl.glue.brahma.service.AttachmentService;
import gl.glue.brahma.util.JsonUtils;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import java.io.UnsupportedEncodingException;

public class SessionController extends Controller {

    private static AttachmentService attachmentService = new AttachmentService();

    @BasicAuth
    @Transactional
    public static Result uploadAttachment(int sessionId) {
        Http.MultipartFormData body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart uploadFilePart = body.getFile("upload");
        if (uploadFilePart == null) {
            return status(400, JsonUtils.simpleError("400", "Missing upload file"));
        }

        int uid = Integer.parseInt(session("id"));
        String filename = uploadFilePart.getFilename();
        try {
            filename = java.net.URLDecoder.decode(filename, "UTF-8");
        } catch (UnsupportedEncodingException e) {}
        Attachment attachment = attachmentService.uploadToSession(
                uid, sessionId, filename, uploadFilePart.getFile());

        if (attachment == null) {
            return status(404, JsonUtils.simpleError("404", "Invalid identifier"));
        }

        // TODO: Send push notification
        System.out.println("URL: " + attachment.getUrl() + " (" + attachment.getFilename() + ")");

        return ok(Json.newObject());
    }

}

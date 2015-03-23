package gl.glue.brahma.controllers.common;

import actions.BasicAuth;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gl.glue.brahma.model.session.Session;
import gl.glue.brahma.service.SessionService;
import gl.glue.brahma.util.JsonUtils;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import gl.glue.brahma.model.attachment.Attachment;
import gl.glue.brahma.service.AttachmentService;
import play.mvc.Http;
import java.io.UnsupportedEncodingException;

public class SessionController extends Controller {

    private static SessionService sessionService = new SessionService();
    private static AttachmentService attachmentService = new AttachmentService();

    /**
     * @api {post} /client/session/chat GetChatHistory
     *
     * @apiGroup Session
     * @apiName GetChatHistory
     * @apiDescription Request chat history for a session
     *
     * @apiParam {Integer}  session Session id.
     *
     * @apiSuccess {Object}     chat Object with the session id array of messages
     * @apiSuccess {Object[]}   chat.id Array with all messages for session id
     * @apiSuccess {Object[]}   sessions Sessions referenced in chat.id
     *
     * @apiSuccessExample {json} Success-Response:
     *      HTTP/1.1 200 OK
     *      {
     *         "chat": {
     *              "90712": [
     *                  {
     *                      message: "Hi!"
     *                  },
     *                  {
     *                      message: "Hello"
     *                  }
     *              ]
     *         },
     *         "session": [
     *              {
     *                  "id": 90700,
     *                  "title": "Consulta 11-02-2015",
     *                  "startDate": 1425312000000,
     *                  "endDate": null,
     *                  "state": "PROGRAMMED",
     *                  "meta": {},
     *                  "timestamp": 1418626800000,
     *                  "isNew": true
     *              }
     *         ]
     *     }
     *
     * @apiError SessionNotFound Not found session identifier in database.
     * @apiErrorExample {json} SessionNotFound
     *      HTTP/1.1 404 Not Found
     *      {
     *          "status": "404",
     *          "title": "Invalid identifier"
     *      }
     *
     * @apiError UserNotLoggedIn User is not logged in.
     * @apiErrorExample {json} UserNotLoggedIn
     *      HTTP/1.1 401 Unauthorized
     *      {
     *          "status": "401",
     *          "title": "You are not logged in"
     *      }
     *
     * @apiVersion 0.1.0
     */
    @BasicAuth
    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public static Result getChatHistory(int sessionId) {
        Session session = sessionService.getById(sessionId);
        if (session == null) return status(404, JsonUtils.simpleError("404", "Invalid identifier"));

        ArrayNode redisMessages = sessionService.getChatMessages(sessionId);
        return ok(Json.newObject()
                .putPOJO("chat", Json.newObject()
                        .putPOJO(String.valueOf(session.getId()), redisMessages))
                .putPOJO("sessions", new ArrayNode(JsonNodeFactory.instance).add(Json.toJson(session))));
    }

    /**
     * @api {post} /client/session/chat AppendChatMessage
     *
     * @apiGroup Session
     * @apiName AppendChatMessage
     * @apiDescription Append message chat history for a session
     *
     * @apiParam {Integer}  session Session id.
     *      {
     *          "sessionId": "M8PEV8LHANHJY",
     *          "messaage": "{ \"miguel\": \"Hola\" }"
     *      }
     *
     * @apiSuccess {Object}     chat Object with the session id array of messages
     * @apiSuccess {Object[]}   chat.id Array with all messages for session id
     * @apiSuccess {Object[]}   sessions Sessions referenced in chat.id
     *
     * @apiSuccessExample {json} Success-Response:
     *      HTTP/1.1 200 OK
     *      {
     *         "chat": {
     *              "90712": [
     *                  {
     *                      message: "Hi!"
     *                  },
     *                  {
     *                      message: "Hello"
     *                  }
     *              ]
     *         },
     *         "session": [
     *              {
     *                  "id": 90700,
     *                  "title": "Consulta 11-02-2015",
     *                  "startDate": 1425312000000,
     *                  "endDate": null,
     *                  "state": "PROGRAMMED",
     *                  "meta": {},
     *                  "timestamp": 1418626800000,
     *                  "isNew": true
     *              }
     *         ]
     *     }
     *
     * @apiError SessionNotFound Not found session identifier in database.
     * @apiErrorExample {json} SessionNotFound
     *      HTTP/1.1 404 Not Found
     *      {
     *          "status": "404",
     *          "title": "Invalid identifier"
     *      }
     *
     * @apiError UserNotLoggedIn User is not logged in.
     * @apiErrorExample {json} UserNotLoggedIn
     *      HTTP/1.1 401 Unauthorized
     *      {
     *          "status": "401",
     *          "title": "You are not logged in"
     *      }
     *
     * @apiError MissingRequiredField Missing required field
     * @apiErrorExample {json} MissingRequiredField
     *      HTTP/1.1 400 BadRequest
     *      {
     *          "status": "400",
     *          "title": "Missing required field"
     *      }
     *
     * @apiVersion 0.1.0
     */
    @BasicAuth
    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public static Result appendChatMessage() {

        JsonNode json = request().body().asJson();

        ObjectNode result = JsonUtils.checkRequiredFields(json, "sessionId", "message");
        if (result != null) return badRequest(result);

        int sessionId = json.findPath("sessionId").asInt();
        String message = json.findPath("message").asText();

        Session session = sessionService.getById(sessionId);
        if (session == null) return status(404, JsonUtils.simpleError("404", "Invalid identifier"));

        ArrayNode redisMessages = sessionService.appendChatMessage(sessionId, message);
        return ok(Json.newObject()
                .putPOJO("chat", Json.newObject()
                        .putPOJO(String.valueOf(session.getId()), redisMessages))
                .putPOJO("sessions", new ArrayNode(JsonNodeFactory.instance).add(Json.toJson(session))));
    }
    @BasicAuth
    @Transactional
    public static Result uploadAttachment(int sessionId) {
        Http.MultipartFormData body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart uploadFilePart = body.getFile("upload");
        Http.MultipartFormData.FilePart uploadFilePart2 = body.getFile("upload_thumb");
        if (uploadFilePart == null) {
            return status(400, JsonUtils.simpleError("400", "Missing upload file"));
        }

        int uid = Integer.parseInt(session("id"));
        String filename = uploadFilePart.getFilename();
        try {
            filename = java.net.URLDecoder.decode(filename, "UTF-8");
        } catch (UnsupportedEncodingException e) {}

        Attachment attachment;
        if (uploadFilePart2 == null) {
            attachment = attachmentService.uploadToSession(
                    uid, sessionId, filename, uploadFilePart.getFile());
        } else {
            attachment = attachmentService.uploadToSession(
                    uid, sessionId, filename, uploadFilePart.getFile(), uploadFilePart2.getFile());
        }

        if (attachment == null) {
            return status(404, JsonUtils.simpleError("404", "Invalid identifier"));
        }

        return ok(Json.newObject()
                .putPOJO("attachments", new ArrayNode(JsonNodeFactory.instance).add(Json.toJson(attachment))));
    }
}

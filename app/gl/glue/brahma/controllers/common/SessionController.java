package gl.glue.brahma.controllers.common;

import actions.BasicAuth;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gl.glue.brahma.model.session.Session;
import gl.glue.brahma.service.SessionService;
import gl.glue.brahma.util.JsonUtils;
import gl.glue.brahma.util.RedisHelper;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import redis.clients.jedis.Jedis;

import java.util.List;

public class SessionController extends Controller {

    private static SessionService sessionService = new SessionService();

    /**
     * @api {post} /client/session/chat GetChatHistory
     *
     * @apiGroup Session
     * @apiName CreateSession
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

        RedisHelper redisHelper = new RedisHelper();
        Jedis redis = redisHelper.getResource();
        String key = redisHelper.generateKey(sessionId);
        int size = redis.llen(key).intValue();

        List<String> redisMessages = redis.lrange(key, 0, size);
        ArrayNode messages = new ArrayNode(JsonNodeFactory.instance);
        for (String redisMessage : redisMessages) {
            try {
                messages.add(Json.parse(redisMessage));
            } catch(RuntimeException e) {
                // prevent parse errors
            }
        }

        return ok(Json.newObject()
                .putPOJO("chat", Json.newObject()
                        .putPOJO(String.valueOf(session.getId()), messages))
                .putPOJO("sessions", new ArrayNode(JsonNodeFactory.instance).add(Json.toJson(session))));
    }
}

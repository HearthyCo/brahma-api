package gl.glue.brahma.controllers.client;

import actions.BasicAuth;
import actions.ClientAuth;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gl.glue.brahma.model.historyentry.HistoryEntry;
import gl.glue.brahma.service.HistoryService;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.List;
import java.util.stream.Collectors;

public class HistoryController extends Controller {

    private static HistoryService historyService = new HistoryService();

    /**
     * @api {get} /client/history/:type History
     *
     * @apiGroup User
     * @apiName GetHistorySection
     * @apiDescription Return the user history section specified
     *
     * @apiParam {String} type Target history entry type
     *
     * @apiSuccess {object[]}    historyentries          Contains a list of entries for the specified section
     * @apiSuccessExample {json} Success-Response
     *      HTTP/1.1 200 OK
     *      {
     *          "allergies": [91100],
     *          "historyentries": [
     *              {
     *                  "id": 91100,
     *                  "title": "Lactosa",
     *                  "timestamp": 1418628600000,
     *                  "removed": false,
     *                  "description": "Insuficiencia cardiorespiratoria.",
     *                  "meta": {
     *                      "rating": 5
     *                  },
     *                  "type": "allergies"
     *              }
     *          ]
     *      }
     *
     * @apiError {Object} UserNotLoggedIn User is not logged in.
     * @apiErrorExample {json} UserNotLoggedIn
     *      HTTP/1.1 401 Unauthorized
     *      {
     *          "status": "401",
     *          "title": "You are not logged in"
     *      }
     *
     * @apiVersion 0.1.0
     */
    @ClientAuth
    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public static Result getHistorySection(String section) {
        int uid = Integer.parseInt(session("id"));

        List<HistoryEntry> historyentries = historyService.getHistorySection(uid, section);
        List<Integer> ids = historyentries.stream().map(o -> o.getId()).collect(Collectors.toList());

        ObjectNode result = Json.newObject();
        result.put("historyentries", Json.toJson(historyentries));
        result.put(section, Json.toJson(ids));

        return ok(result);
    }
}

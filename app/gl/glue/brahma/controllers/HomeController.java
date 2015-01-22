package gl.glue.brahma.controllers;

import actions.BasicAuth;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gl.glue.brahma.service.HomeService;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

public class HomeController extends Controller {

    private static HomeService homeService = new HomeService();

    /**
     * @api {get} /user/home Homepage
     *
     * @apiGroup User
     * @apiName GetHome
     * @apiDescription Collect all entities required to show in home view.
     *
     * @apiSuccess {Object}     sessions Contains all user sessions grouped by state.
     * @apiSuccess {Object[]}   sessions.programmed Contents all user sessions in programmed state.
     * @apiSuccess {Object[]}   sessions.underway Contents all user sessions in underway state.
     * @apiSuccess {Object[]}   sessions.closed Contents all user sessions in closed state.
     * @apiSuccessExample {json} Success-Response
     *      HTTP/1.1 200 OK
     *      {
     *          "sessions": {
     *              "programmed": [
     *                  {
     *                      "id": 90700,
     *                      "title": "testSession1",
     *                      "startDate": 1425312000000,
     *                      "endDate": 1425312900000,
     *                      "state": "PROGRAMMED",
     *                      "meta": {},
     *                      "timestamp": 1418626800000,
     *                      "isNew": true
     *                    }
     *              ],
     *              "underway": [],
     *              "closed": [
     *                  {
     *                      "id": 90702,
     *                      "title": "testSession3",
     *                      "startDate": 1425384000000,
     *                      "endDate": 1425208500000,
     *                      "state": "CLOSED",
     *                      "meta": {},
     *                      "timestamp": 1418666400000,
     *                      "isNew": true
     *                  }
     *              ]
     *          }
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
    @BasicAuth
    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public static Result get() {
        int uid = Integer.parseInt(session("id"));

        // Get session with login
        ObjectNode sessions = homeService.getSessions(uid);

        ObjectNode result = Json.newObject();
        result.put("sessions", sessions);

        return ok(result);
    }
}

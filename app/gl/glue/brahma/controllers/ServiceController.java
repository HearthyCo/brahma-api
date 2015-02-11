package gl.glue.brahma.controllers;

import actions.BasicAuth;
import gl.glue.brahma.service.ServiceService;
import play.db.jpa.Transactional;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

public class ServiceController extends Controller {

    private static ServiceService serviceService = new ServiceService();

    /**
     * @api {get} /services Services
     *
     * @apiGroup Services
     * @apiName GetService
     * @apiDescription Return all services in database
     *
     * @apiSuccess {object}     services Object with all services in database
     * @apiSuccessExample {json} Success-Response:
     *     HTTP/1.1 200 OK
     *     {
     *          services: {
     *              "general": [
     *                  {
     *                      "name": "Chat",
     *                      "mode": "ASYNC",
     *                      "price": 250
     *                  },
     *                  {
     *                      "name": "Video Session",
     *                      "mode": "VIDEO",
     *                      "price": 1500
     *                  }
     *              ]
     *          }
     *     }
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
    public static Result getServices() {
        return ok(serviceService.getServices());
    }
}

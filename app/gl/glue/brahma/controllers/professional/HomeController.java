package gl.glue.brahma.controllers.professional;

import play.mvc.Controller;

public class HomeController extends Controller {

    /**
     * @api {get} /user/home Homepage
     *
     * @apiGroup Professional
     * @apiName GetHome
     * @apiDescription Collect all entities required to show in home view.
     *
     * @apiSuccess {Object}     sessions             Contains all user sessions grouped by state.
     * @apiSuccessExample {json} Success-Response
     *      HTTP/1.1 200 OK
     *      {
     *          "sessions": {
     *
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
     * @apiError {Object} UnauthorizedUser User is not a professional.
     * @apiErrorExample {json} UnauthorizedUser
     *      HTTP/1.1 403 Unauthorized
     *      {
     *          "status": "403",
     *          "title": "Unauthorized"
     *      }
     *
     * @apiVersion 0.1.0
     */
    /*
    @ProfessionalAuth
    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public static Result getHome() {
        int uid = Integer.parseInt(session("id"));

        ObjectNode result = Json.newObject();

        return ok(result);
    }
    */
}

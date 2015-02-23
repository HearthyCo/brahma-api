package gl.glue.brahma.controllers.common;

import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

public class UserController extends Controller {

    /**
     * @api {post} /user/logout Logout
     *
     * @apiGroup User
     * @apiName Logout
     * @apiDescription Destroy user session.
     *
     * @apiSuccessExample {json} Success-Response
     *      HTTP/1.1 200 OK
     *      {
     *      }
     *
     * @apiVersion 0.1.0
     */
    @Transactional
    public static Result logout() {
        session().clear();
        return ok(Json.newObject());
    }

}
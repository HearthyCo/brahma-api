package actions;

import gl.glue.brahma.util.JsonUtils;
import play.libs.F;
import play.mvc.Action;
import play.mvc.Http;

public class BasicAuthAction extends Action.Simple {

    @Override
    public F.Promise call(Http.Context ctx) throws Throwable {
        // Check if login
        if(ctx.session().get("id") == null) {
            return F.Promise.pure(status(401, JsonUtils.simpleError("401", "You are not logged in")));
        }

        return delegate.call(ctx);
    }
}
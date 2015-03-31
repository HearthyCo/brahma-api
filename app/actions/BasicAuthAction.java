package actions;

import gl.glue.brahma.util.CORSResult;
import gl.glue.brahma.util.JsonUtils;
import play.libs.F;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

import static play.libs.F.Promise.pure;

public class BasicAuthAction extends Action.Simple {

    protected F.Promise failWith(Http.Context ctx, int status, String message) {
        return pure(new CORSResult(ctx.request(), status, JsonUtils.simpleError(Integer.toString(status), message)));
    }

    @Override
    public F.Promise<Result> call(Http.Context ctx) throws java.lang.Throwable {

        // Check if login
        if(ctx.session().get("id") == null) {
            return failWith(ctx, 401, "You are not logged in");
        }

        return delegate.call(ctx);
    }

}
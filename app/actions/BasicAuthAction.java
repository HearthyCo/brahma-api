package actions;

import play.libs.F;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

public class BasicAuthAction extends Action.Simple {

    public F.Promise<Result> call(Http.Context ctx) throws Throwable {
        // Check if login
        //if(ctx.session().get("id") == null) return unauthorized("You are not logged in");
        return delegate.call(ctx);
    }
}

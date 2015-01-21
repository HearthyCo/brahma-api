package actions;


import play.Logger;
import play.libs.F;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

public class BasicAuthAction extends Action.Simple {

    public F.Promise<Result> call(Http.Context ctx) throws Throwable {
        Logger.info("Calling action for login " + ctx.session());
        return delegate.call(ctx);
    }
}

package actions;

import play.libs.F;
import play.mvc.Http;

public class ClientAuthAction extends BasicAuthAction {

    @Override
    public F.Promise call(Http.Context ctx) throws Throwable {
        // Check if login
        if(ctx.session().get("id") == null) {
            return failWith(ctx, 401, "You are not logged in");
        }

        // Check if client
        String role = ctx.session().get("role");
        if(role == null || !role.equals("client")) {
            return failWith(ctx, 403, "Unauthorized");
        }

        return delegate.call(ctx);
    }
}
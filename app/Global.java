import com.fasterxml.jackson.databind.JsonNode;
import gl.glue.brahma.util.JsonUtils;
import play.Configuration;
import play.GlobalSettings;
import play.Play;
import play.core.j.JavaResults;
import play.db.jpa.JPA;
import play.libs.F.Promise;
import play.libs.Scala;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.List;

import static play.libs.F.Promise.pure;

public class Global extends GlobalSettings {

    private Configuration conf;
    private Configuration getConf() {
        if (conf == null) conf = Play.application().configuration();
        return conf;
    }

    // For CORS
    private class ActionWrapper extends Action.Simple {
        public ActionWrapper(Action<?> action) {
            this.delegate = action;
        }

        @Override
        public Promise<Result> call(Http.Context ctx) throws java.lang.Throwable {
            Http.Response response = ctx.response();
            String origin = ctx.request().getHeader("Origin");
            if (getConf().getStringList("cors.origins").contains(origin)) {
                response.setHeader("Access-Control-Allow-Origin", origin);
                response.setHeader("Access-Control-Allow-Credentials", "true");
            }

            if (getConf().getBoolean("persistence.rollbackOnly", false)) {
                JPA.em().getTransaction().setRollbackOnly();
            }
            Promise<Result> result = this.delegate.call(ctx);
            return result;
        }
    }

    @Override
    public Action<?> onRequest(Http.Request request, java.lang.reflect.Method actionMethod) {
        return new ActionWrapper(super.onRequest(request, actionMethod));
    }

    private class CORSResult implements Result {
        final private play.api.mvc.Result wrappedResult;

        public CORSResult(Http.RequestHeader request, int statusCode, JsonNode body) {
            List<Tuple2<String, String>> list = new ArrayList<>();
            if (request.hasHeader("Origin")) {
                String origin = request.getHeader("Origin");
                if (getConf().getStringList("cors.origins").contains(origin)) {
                    list.add(new Tuple2<>("Access-Control-Allow-Origin", origin));
                    list.add(new Tuple2<>("Access-Control-Allow-Credentials", "true"));
                }
            }
            list.add(new Tuple2<>("Content-Type", "application/json; charset=utf-8"));
            wrappedResult = JavaResults.Status(statusCode)
                    .apply(body.toString().getBytes(), JavaResults.writeBytes())
                    .withHeaders(Scala.toSeq(list));
        }

        public play.api.mvc.Result toScala() {
            return this.wrappedResult;
        }
    }

    @Override
    public Promise<Result> onBadRequest(Http.RequestHeader request, String error) {
        String msg = (getConf().getBoolean("errors.detailed", false)) ? error : "Bad Request";
        return pure(new CORSResult(request, 400, JsonUtils.simpleError("400", msg)));
    }

    @Override
    public Promise<Result> onError(Http.RequestHeader request, Throwable t) {
        String msg = (getConf().getBoolean("errors.detailed", false)) ? t.getMessage() : "Internal Server Error";
        return pure(new CORSResult(request, 400, JsonUtils.simpleError("500", t.getMessage())));
    }

    @Override
    public Promise<Result> onHandlerNotFound(Http.RequestHeader request) {
        return pure(new CORSResult(request, 400, JsonUtils.simpleError("404", "Not Found")));
    }
}
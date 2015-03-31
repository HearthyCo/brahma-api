package gl.glue.brahma.util;

import com.fasterxml.jackson.databind.JsonNode;
import play.Configuration;
import play.Play;
import play.core.j.JavaResults;
import play.libs.Scala;
import play.mvc.Http;
import play.mvc.Result;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.List;

public class CORSResult implements Result {

    final private play.api.mvc.Result wrappedResult;
    private Configuration conf = Play.application().configuration();

    public CORSResult(Http.RequestHeader request, int statusCode, JsonNode body) {
        List<Tuple2<String, String>> list = new ArrayList<>();
        if (request.hasHeader("Origin")) {
            String origin = request.getHeader("Origin");
            if (conf.getStringList("cors.origins").contains(origin)) {
                list.add(new Tuple2<>("Access-Control-Allow-Origin", origin));
                list.add(new Tuple2<>("Access-Control-Allow-Credentials", "true"));
            }
        }
        list.add(new Tuple2<>("Content-Type", "application/json; charset=utf-8"));
        wrappedResult = JavaResults.Status(statusCode)
                .apply(body.toString().getBytes(), JavaResults.writeBytes())
                .withHeaders(Scala.toSeq(list));
    }

    @Override
    public play.api.mvc.Result toScala() {
        return this.wrappedResult;
    }

}
package gl.glue.brahma.controllers.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.wordnik.swagger.annotations.*;
import gl.glue.brahma.util.JsonUtils;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

@Api(value = "/client", description = "User client functions")
public class PushController extends Controller {

    @ApiOperation(nickname = "update", value = "Update subscription.",
            notes = "Updates a device subscription to the Push service.",
            httpMethod = "POST")
    @ApiImplicitParams(value= {
            @ApiImplicitParam(name = "body", defaultValue = "{\n" +
                    "  \"token\": \"APA91...\",\n" +
                    "  \"proto\": \"gcm\"\n" +
                    "}", value="Object with post params", required = true, dataType = "Object", paramType = "body")})
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Params has not a required field.")})
    @BodyParser.Of(BodyParser.Json.class)
    public static Result update() {
        JsonNode json = request().body().asJson();

        ObjectNode result = JsonUtils.checkRequiredFields(json, "token", "proto");
        if (result != null) return badRequest(result);

        ObjectNode msg = Json.newObject();
        msg.put("token", json.findPath("token").asText());
        msg.put("proto", json.findPath("proto").asText());
        if (session("id") != null) msg.put("uid", Integer.parseInt(session("id")));
        if (json.has("lang")) msg.put("lang", json.findPath("lang").asText());
        gl.glue.play.amqp.Controller.sendMessage("push.update", msg.toString());

        return ok(Json.newObject());
    }


}

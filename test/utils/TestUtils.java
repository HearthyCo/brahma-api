package utils;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.FluentIterable;
import gl.glue.brahma.util.JsonUtils;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.FakeRequest;
import scala.Option;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static play.test.Helpers.*;


public class TestUtils {

    private static final int REQUEST_TIMEOUT = 1000;

    public static Result makeClientLoginRequest(String login, String pass) {
        return makeLoginRequest(login, pass, "client");
    }
    public static Result makeProfessionalLoginRequest(String login, String pass) {
        return makeLoginRequest(login, pass, "professional");
    }

    public static Result makeLoginRequest(String login, String pass, String type) {
        ObjectNode user = Json.newObject();
        user.put("email", login);
        user.put("password", pass);

        FakeRequest fr = fakeRequest(POST, "/v1/" + type + "/login").withJsonBody(user);
        Result result = routeAndCall(fr, REQUEST_TIMEOUT);
        assertNotNull(result);
        return result;
    }

    public static ObjectNode toJson(Result result) {
        ObjectNode ret = JsonUtils.result2json(result);
        assertNotNull(ret);
        return ret;
    }

    public static boolean hasCookies(Result result) {
        Option<String> cookie = result.toScala().header().headers().get("Set-Cookie");
        return !cookie.isEmpty() && !cookie.get().equals("");
    }

    public static void assertError(int status, ObjectNode ret) {
        assertTrue(ret.has("errors"));
        assertEquals(Integer.toString(status), ret.get("errors").get(0).get("status").asText());
    }


    public static Result callController(String method, String url, Result auth) {
        return callController(method, url, auth, null);
    }
    public static Result callController(String method, String url, Result auth, ObjectNode data) {
        Http.Cookie[] cookies = FluentIterable.from(cookies(auth)).toArray(Http.Cookie.class);
        FakeRequest fr = fakeRequest(method, url).withCookies(cookies);
        if (data != null) fr.withJsonBody(data);
        return routeAndCall(fr, REQUEST_TIMEOUT);
    }

}

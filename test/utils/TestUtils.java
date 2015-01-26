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

    public static Result makeLoginRequest(String login, String pass) {
        ObjectNode user = Json.newObject();
        user.put("login", login);
        user.put("password", pass);

        FakeRequest fr = fakeRequest(POST, "/v1/user/login").withJsonBody(user);
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

    public static void assertError(ObjectNode ret, int status) {
        assertTrue(ret.has("errors"));
        assertEquals(Integer.toString(status), ret.get("errors").get(0).get("status").asText());
    }

    public static Result getTransactionRequest(Result responseLogin) {
        Http.Cookie[] cookies = FluentIterable.from(cookies(responseLogin)).toArray(Http.Cookie.class);

        FakeRequest fr = fakeRequest(GET, "/v1/user/balance").withCookies(cookies);
        return routeAndCall(fr, REQUEST_TIMEOUT);
    }

    public static Result getServicesRequest(Result responseLogin) {
        Http.Cookie[] cookies = FluentIterable.from(cookies(responseLogin)).toArray(Http.Cookie.class);

        FakeRequest fr = fakeRequest(GET, "/v1/services").withCookies(cookies);
        return routeAndCall(fr, REQUEST_TIMEOUT);
    }


    public static Result getHomeRequest(Result responseLogin) {
        Http.Cookie[] cookies = FluentIterable.from(cookies(responseLogin)).toArray(Http.Cookie.class);

        FakeRequest fr = fakeRequest(GET, "/v1/user/home").withCookies(cookies);
        return routeAndCall(fr, REQUEST_TIMEOUT);
    }

    public static Result getSessionRequest(int id, Result responseLogin) {
        Http.Cookie[] cookies = FluentIterable.from(cookies(responseLogin)).toArray(Http.Cookie.class);

        FakeRequest fr = fakeRequest(GET, "/v1/session/" + id).withCookies(cookies);
        return routeAndCall(fr, REQUEST_TIMEOUT);
    }

    public static Result getSessionStateRequest(String state, Result responseLogin) {
        Http.Cookie[] cookies = FluentIterable.from(cookies(responseLogin)).toArray(Http.Cookie.class);

        FakeRequest fr = fakeRequest(GET, "/v1/user/sessions/" + state).withCookies(cookies);
        return routeAndCall(fr, REQUEST_TIMEOUT);
    }
}

package gl.glue.brahma.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.FluentIterable;
import gl.glue.brahma.util.JsonUtils;
import org.junit.Test;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.FakeRequest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static play.test.Helpers.*;

public class HomeControllerTest extends TransactionalTest {

    private final int REQUEST_TIMEOUT = 1000;

    private ObjectNode toJson(Result result) {
        ObjectNode ret = JsonUtils.result2json(result);
        assertNotNull(ret);
        return ret;
    }

    private Result makeLoginRequest(String login, String pass) {
        ObjectNode user = Json.newObject();
        user.put("login", login);
        user.put("password", pass);

        FakeRequest fr = fakeRequest(POST, "/v1/user/login").withJsonBody(user);
        Result result = routeAndCall(fr, REQUEST_TIMEOUT);
        assertNotNull(result);
        return result;
    }

    private Result getHomeRequest(Result responseLogin) {
        Http.Cookie[] cookies = FluentIterable.from(cookies(responseLogin)).toArray(Http.Cookie.class);

        FakeRequest fr = fakeRequest(GET, "/v1/user/home").withCookies(cookies);
        return routeAndCall(fr, REQUEST_TIMEOUT);
    }

    @Test // Request without user authentication
    public void requestHomeWithoutAuthentication() {
        FakeRequest fr = fakeRequest(GET, "/v1/user/home");
        Result result = routeAndCall(fr, REQUEST_TIMEOUT);
        assertNotNull(result);
        assertEquals(401, result.toScala().header().status());
    }

    @Test // Request home
    public void requestHomeOk() {
        String login = "testClient1";
        Result responseLogin = makeLoginRequest(login, login);

        Result result = getHomeRequest(responseLogin);
        assertNotNull(result);
        assertEquals(200, result.toScala().header().status());

        ObjectNode ret = toJson(result);
        JsonNode programmed = ret.get("sessions").get("programmed");
        JsonNode closed = ret.get("sessions").get("closed");

        assertEquals(programmed.size(), 1);
        assertEquals(programmed.get(0).get("id").asInt(), 90700);

        assertEquals(closed.size(), 2);
        assertEquals(closed.get(0).get("id").asInt(), 90702);
    }
}

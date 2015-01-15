package gl.glue.brahma.test;

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
import static org.junit.Assert.assertNull;
import static play.test.Helpers.*;

public class SessionControllerTest extends TransactionalTest {

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

    private Result getSessionRequest(int id, Result responseLogin) {
        Http.Cookie[] cookies = FluentIterable.from(cookies(responseLogin)).toArray(Http.Cookie.class);

        FakeRequest fr = fakeRequest(GET, "/v1/session/" + id).withCookies(cookies);
        return routeAndCall(fr, REQUEST_TIMEOUT);
    }

    @Test // Request without id param
    public void returnSessionWithoutParams() {
        String login = "testClient1";
        Result responseLogin = makeLoginRequest(login, login);

        Http.Cookie[] cookies = FluentIterable.from(cookies(responseLogin)).toArray(Http.Cookie.class);

        FakeRequest fr = fakeRequest(GET, "/v1/session/").withCookies(cookies);
        Result result = routeAndCall(fr, REQUEST_TIMEOUT);
        assertNull(result);
    }


    @Test // Request without user authentication
    public void returnSessionWithoutAuthentication() {
        int id = 90700;
        FakeRequest fr = fakeRequest(GET, "/v1/session/" + id);
        Result result = routeAndCall(fr, REQUEST_TIMEOUT);
        assertNotNull(result);
        assertEquals(401, result.toScala().header().status());
    }

    @Test // Request with invalid user Authentication. User "testClient2" is not an user for session 90700
    public void returnSessionWithInvalidAuthentication() {
        String login = "testClient2";
        Result responseLogin = makeLoginRequest(login, login);

        Result result = getSessionRequest(90700, responseLogin);
        assertNotNull(result);
        assertEquals(404, result.toScala().header().status());
    }

    @Test // Request with an non-existent session id
    public void returnSessionInvalidId() {
        String login = "testClient1";
        Result responseLogin = makeLoginRequest(login, login);

        Result result = getSessionRequest(0, responseLogin);
        assertNotNull(result);
        assertEquals(404, result.toScala().header().status());
    }

    @Test // Valid request
    public void returnSessionOk() {
        String login = "testClient1";
        Result responseLogin = makeLoginRequest(login, login);

        int id = 90700;
        Result result = getSessionRequest(id, responseLogin);
        ObjectNode ret = toJson(result);

        assertNotNull(result);
        assertEquals(200, result.toScala().header().status());
        assertEquals(id, ret.get("session").get("id").asInt());
    }
}

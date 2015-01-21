package gl.glue.brahma.test;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.FluentIterable;
import gl.glue.brahma.service.SessionService;
import org.junit.Test;
import play.Logger;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.FakeRequest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static play.test.Helpers.*;

public class HomeControllerTest extends TransactionalTest {

    private final int REQUEST_TIMEOUT = 1000;

    private SessionService sessionService = new SessionService();

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

    @Test // Request with invalid user Authentication. User "testClient2" is not an user for session 90700
    public void requestHomeOk() {
        String login = "testClient1";
        Result responseLogin = makeLoginRequest(login, login);

        Result result = getHomeRequest(responseLogin);
        assertNotNull(result);
        assertEquals(200, result.toScala().header().status());

        Logger.info("RESULT" + Json.toJson(result));
    }
}

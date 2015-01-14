package gl.glue.brahma.test;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.FluentIterable;
import gl.glue.brahma.model.session.Session;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.FakeRequest;

import static org.junit.Assert.assertNotNull;
import static play.test.Helpers.*;

public class SessionControllerTest extends TransactionalTest {

    private final int REQUEST_TIMEOUT = 1000;

    private Result makeLoginRequest(String login, String pass) {
        ObjectNode user = Json.newObject();
        user.put("login", login);
        user.put("password", pass);

        FakeRequest fr = fakeRequest(POST, "/v1/user/login").withJsonBody(user);
        Result result = routeAndCall(fr, REQUEST_TIMEOUT);
        assertNotNull(result);
        return result;
    }

    @test
    private void returnSessionWithoutParams() {

    }

    @test
    private void returnSessionWithoutAuthentication() {

    }

    @test
    private void returnSessionWithInvalidAuthentication() {

    }

    @test
    private void returnSessionInvalidId() {

    }

    @test
    private void returnSessionValidId() {
        String login = "testClient1";
        Result result = makeLoginRequest(login, login);

        int id = 90700;
        Http.Cookie[] cookies = FluentIterable.from(cookies(result)).toArray(Http.Cookie.class);

        FakeRequest fr = fakeRequest(GET, "/v1/session/" + id).withCookies(cookies);
        Result result = routeAndCall(fr, REQUEST_TIMEOUT);
        assertNotNull(result);

    }
}

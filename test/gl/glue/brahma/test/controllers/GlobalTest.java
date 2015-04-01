package gl.glue.brahma.test.controllers;

import org.junit.Test;
import play.api.http.MediaRange;
import play.api.http.MediaType;
import play.api.mvc.*;
import play.libs.Json;
import play.mvc.Result;
import play.test.FakeRequest;
import scala.Option;
import scala.collection.Seq;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import utils.TransactionalTest;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static play.mvc.Http.Status.REQUEST_TIMEOUT;
import static play.test.Helpers.*;

public class GlobalTest extends TransactionalTest {

    private final String origin = "http://localhost:3000";

    private void checkHeaders(play.api.mvc.Result result) {
        Map<String, String> expectedHeaders = new TreeMap<>();
        expectedHeaders.put("Content-Type", "application/json; charset=utf-8");
        expectedHeaders.put("Access-Control-Allow-Origin", origin);
        expectedHeaders.put("Access-Control-Allow-Credentials", "true");
        scala.collection.immutable.Map<String, String> headers = result.header().headers();
        System.out.println(headers.toString());
        for (Map.Entry<String, String> entry : expectedHeaders.entrySet()) {
            Option<String> contentType = headers.get(entry.getKey());
            assertTrue(contentType.nonEmpty());
            assertEquals(entry.getValue(), contentType.get());
        }
    }

    private play.api.mvc.Result redeem(Future<play.api.mvc.Result> future) {
        try {
            return Await.result(future, Duration.create(100, TimeUnit.MILLISECONDS));
        } catch (Exception e) {
            return null;
        }
    }


    @Test
    public void testNotFoundIsJson() {
        FakeRequest fr = fakeRequest(GET, "/404-not-found").withHeader("Origin", origin);
        // We cannot use routeAndCall(fr, REQUEST_TIMEOUT) as it doesn't take Global into account.
        // See https://github.com/playframework/playframework/issues/2484
        // So we're using the fakeRequests for the headers only.
        play.api.mvc.Result result = redeem(app.global().onHandlerNotFound(fr.getWrappedRequest()));
        assertNotNull(result);
        assertEquals(404, result.header().status());
        checkHeaders(result);
    }

    @Test
    public void testBadRequestIsJson() {
        FakeRequest fr = fakeRequest(GET, "/v1/client/login").withHeader("Origin", origin);
        play.api.mvc.Result result = redeem(app.global().onBadRequest(fr.getWrappedRequest(), "Test Reason"));
        assertNotNull(result);
        assertEquals(400, result.header().status());
        checkHeaders(result);
    }

    @Test
    public void testInternalServerErrorIsJson() {
        FakeRequest fr = fakeRequest(GET, "/500-error").withHeader("Origin", origin);
        play.api.mvc.Result result = redeem(app.global().onError(fr.getWrappedRequest(), new RuntimeException("Test")));
        assertNotNull(result);
        assertEquals(500, result.header().status());
        checkHeaders(result);
    }

    @Test
    public void testUnauthorizedIsJson() {
        FakeRequest fr = fakeRequest(GET, "/v1/client/me/home").withHeader("Origin", origin);
        fr.withJsonBody(Json.newObject());
        Result result = routeAndCall(fr, REQUEST_TIMEOUT);
        assertNotNull(result);
        assertEquals(401, result.toScala().header().status());
        checkHeaders(result.toScala());
    }


}

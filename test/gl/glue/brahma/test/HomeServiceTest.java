package gl.glue.brahma.test;

import com.fasterxml.jackson.databind.node.ObjectNode;
import gl.glue.brahma.service.HomeService;
import org.junit.Test;
import play.Logger;

public class HomeServiceTest extends TransactionalTest {

    private HomeService homeService = new HomeService();

    @Test // Request with invalid user Authentication. User "testClientDummy" not exists
    public void returnSessionsWithInvalidAuthentication() {
        String login = "testClientDummy";
        ObjectNode result = homeService.getSessions(login);
        //assertEquals(null, result);
    }

    @Test // Request sessions with valid user Authentication
    public void returnSessionsOn() {
        String login = "testClient1";
        ObjectNode result = homeService.getSessions(login);
        Logger.info("RESULT" + result);
    }
}

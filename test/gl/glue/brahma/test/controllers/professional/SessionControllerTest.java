package gl.glue.brahma.test.controllers.professional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.junit.Test;
import play.Logger;
import play.libs.Json;
import utils.TransactionalTest;

import play.mvc.Result;
import utils.TestUtils;

import java.util.List;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static play.test.Helpers.*;

public class SessionControllerTest extends TransactionalTest {

    //@Test
    public void testAssignSessionInvalidService() {
        String login = "testProfessional1@glue.gl";
        Result auth = TestUtils.makeProfessionalLoginRequest(login, login);

        // We assign an requested session
        ObjectNode serviceType = Json.newObject();
        serviceType.put("serviceType", 1);

        Result result = TestUtils.callController(POST, "/v1/professional/session/assignPool", auth, serviceType);
        assertNotNull(result);
        assertEquals(404, result.toScala().header().status());
    }

    //@Test
    public void testAssignSession() {
        String login = "testProfessional1@glue.gl";
        Result auth = TestUtils.makeProfessionalLoginRequest(login, login);

        // We assign an requested session
        ObjectNode serviceType = Json.newObject();
        serviceType.put("serviceType", 90302);

        Result resultAssign = TestUtils.callController(POST, "/v1/professional/session/assignPool", auth, serviceType);
        assertNotNull(resultAssign);

        ObjectNode retAssign = TestUtils.toJson(resultAssign);

        assertEquals(200, resultAssign.toScala().header().status());
        assertEquals(1, retAssign.size());
        assertEquals(90713, retAssign.get("sessions").get("id").asInt());

        // Now, we check if
        Result resultAssigned = TestUtils.callController(GET, "/v1/professional/sessions/assigned/90302", auth);
        ObjectNode retAssigned = TestUtils.toJson(resultAssigned);

        assertNotNull(resultAssigned);
        assertEquals(200, resultAssigned.toScala().header().status());
        assertEquals(1, retAssigned.get("sessions").size());
    }

    //@Test
    public void testGetAssignedSessions() {
        String login = "testProfessional1@glue.gl";
        Result auth = TestUtils.makeProfessionalLoginRequest(login, login);

        // First, check without sessions assigned
        Result result = TestUtils.callController(GET, "/v1/professional/sessions/assigned/90302", auth);
        ObjectNode ret = TestUtils.toJson(result);

        assertNotNull(result);
        assertEquals(200, result.toScala().header().status());
        assertEquals(0, ret.size());

        // We assign an requested session
        ObjectNode serviceType = Json.newObject();
        serviceType.put("serviceType", 90302);

        Result resultAssign = TestUtils.callController(POST, "/v1/professional/session/assignPool", auth, serviceType);
        assertNotNull(resultAssign);

        ObjectNode retAssign = TestUtils.toJson(resultAssign);

        assertEquals(200, resultAssign.toScala().header().status());
        assertEquals(1, retAssign.size());
        assertEquals(90713, retAssign.get("sessions").get("id").asInt());

        // Now, we check if
        Result resultAssigned = TestUtils.callController(GET, "/v1/professional/sessions/assigned/90302", auth);
        ObjectNode retAssigned = TestUtils.toJson(resultAssigned);

        assertNotNull(resultAssigned);
        assertEquals(200, resultAssigned.toScala().header().status());
        assertEquals(1, retAssigned.get("sessions").size());
    }

    //@Test // Valid request // TODO DEBUG ERROR
    public void testGetPoolsSize() {
        String login = "testProfessional1@glue.gl";
        Result auth = TestUtils.makeProfessionalLoginRequest(login, login);

        Result result = TestUtils.callController(GET, "/v1/professional/session/pools", auth);
        ObjectNode ret = TestUtils.toJson(result);

        assertNotNull(result);
        assertEquals(200, result.toScala().header().status());
        assertEquals(1, ret.get("pools").get("90302").asInt());
    }

    @Test
    public void testAppendChatOk() {
        String login = "testProfessional1@glue.gl";
        Result auth = TestUtils.makeProfessionalLoginRequest(login, login);

        int id = 90714;
        String message = "Hola pisicola";
        String messageString = "{ \"message\": \"" + message + "\" }";

        ObjectNode messageObject = Json.newObject();
        messageObject.put("sessionId", 90714);
        messageObject.put("message", messageString);

        Result resultAppend = TestUtils.callController(POST, "/v1/professional/session/chat/append", auth, messageObject);
        ObjectNode retAppend = TestUtils.toJson(resultAppend);

        Logger.info("APPEND " + retAppend);

        ArrayNode chatMessageList = (ArrayNode) retAppend.get("chat").get(String.valueOf(id));
        for(JsonNode chatMessage : chatMessageList) {
            assertEquals(message, chatMessage.get("message").asText());
        }
    }
}
package gl.glue.brahma.test.service;

import com.fasterxml.jackson.databind.JsonNode;
import gl.glue.brahma.model.user.Client;
import gl.glue.brahma.model.user.User;
import gl.glue.brahma.service.UserService;
import play.libs.Json;
import utils.TransactionalTest;
import org.junit.Test;
import play.db.jpa.JPA;

import javax.persistence.PersistenceException;
import java.util.Date;

import static org.junit.Assert.*;

public class UserServiceTest extends TransactionalTest {

    private UserService userService = new UserService();

    private User getRegisteringUser(String login, String pass) {
        User user = new Client();
        user.setLogin(login);
        user.setEmail(login);
        user.setPassword(pass);
        user.setBirthdate(new Date());
        user.setGender(User.Gender.OTHER);
        user.setName("Test User");
        return user;
    }

    @Test
    public void testRegisterClientOk() {
        String login = "testNonexistentUser@glue.gl";
        String pass = "anyPassword";
        User user = getRegisteringUser(login, pass);
        User ret = userService.register(user);
        assertNotNull(ret);
        assertEquals(login, ret.getLogin());
        assertTrue(ret.authenticate(pass));
        assertNotNull(userService.login(login, pass));
    }

    @Test
    public void testRegisterClientDupe() {
        String login = "testNonexistentUser@glue.gl";
        String pass = "anyPassword";
        User user = getRegisteringUser(login, pass);
        User ret = userService.register(user);
        assertNotNull(ret);
        JPA.em().flush(); // Make sure no exceptions would have been thrown at end of transaction

        User user2 = getRegisteringUser(login, pass);
        boolean gotException = false;
        try {
            User ret2 = userService.register(user2);
            JPA.em().flush(); // Make sure we get the exception now if it would have been thrown later
        } catch (PersistenceException e) {
            gotException = true;
        }
        assertTrue(gotException);
    }

    @Test
    public void testLoginOk() {
        String login = "testClient1@glue.gl";
        User ret = userService.login(login, login);
        assertNotNull(ret);
        assertEquals(login.toLowerCase(), ret.getEmail());
    }

    @Test
    public void testLoginBadPass() {
        User ret = userService.login("testClient1@glue.gl", "bad-password");
        assertNull(ret);
    }

    @Test
    public void testLoginBadUser() {
        User ret = userService.login("testNonexistentUser@glue.gl", "anyPassword");
        assertNull(ret);
    }

    @Test
    public void testLoginBlockedUser() {
        User ret = userService.login("testPet1@glue.gl", "testPet1");
        assertNull(ret);
    }

    @Test
    public void testConfirmMailOk() {
        boolean confirm = userService.confirmMail(90013, "mbqbvTRBFwr6IaU8kgNCMFWwwc1fSxnj");
        assertTrue(confirm);
        User user = userService.getByEmail("testClientUnconfirmed@glue.gl");
        assertEquals(User.State.CONFIRMED, user.getState());
    }

    @Test
    public void testConfirmMailBadHash() {
        boolean confirm = userService.confirmMail(90013, "bad-hash");
        assertFalse(confirm);
        User user = userService.getByEmail("testClientUnconfirmed@glue.gl");
        assertEquals(User.State.UNCONFIRMED, user.getState());
    }

    @Test
    public void testRequestPasswordChangeOk() {
        User user = userService.requestPasswordChange("testclient1@glue.gl");
        JsonNode meta = Json.parse(user.getMeta().toString()); // Prevent POJOs from messing with us
        String hash = meta.get("confirm").get("password").get("hash").asText();
        assertNotNull(hash);
    }

    @Test
    public void testConfirmPasswordChangeOk() {
        String email = "testClientUnconfirmed@glue.gl";
        String newPass = "new-password";
        boolean res = userService.confirmPasswordChange(90013, "mbqbvTRBFwr6IaU8kgNCMFWwwc1fSxnj", newPass);
        assertTrue(res);
        User login = userService.login(email, newPass);
        assertNotNull(login);
    }

}

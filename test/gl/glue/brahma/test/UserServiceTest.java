package gl.glue.brahma.test;

import gl.glue.brahma.model.user.Client;
import gl.glue.brahma.model.user.User;
import gl.glue.brahma.service.UserService;
import org.junit.Test;

import javax.persistence.PersistenceException;
import javax.validation.ConstraintViolationException;
import java.util.Date;

import static org.junit.Assert.*;

public class UserServiceTest extends TransactionalTest {

    private UserService userService = new UserService();

    private User getRegisteringUser(String login, String pass) {
        User user = new Client();
        user.setLogin(login);
        user.setPassword(pass);
        user.setBirthdate(new Date());
        user.setGender(User.Gender.OTHER);
        user.setName("Test User");
        user.setMeta("{}");
        user.setCanLogin(true);
        return user;
    }

    @Test
    public void testRegisterClientOk() {
        String login = "testNonexistentUser";
        String pass = "anyPassword";
        User user = getRegisteringUser(login, pass);
        User ret = userService.register(user);
        assertNotNull(ret);
        assertEquals(ret.getLogin(), login);
        assertTrue(ret.authenticate(pass));
        assertNotNull(userService.login(login, pass));
    }

    @Test
    public void testRegisterClientDupe() {
        String login = "testNonexistentUser";
        String pass = "anyPassword";
        User user = getRegisteringUser(login, pass);
        User ret = userService.register(user);
        assertNotNull(ret);

        User user2 = getRegisteringUser(login, pass);
        boolean gotException = false;
        try {
            User ret2 = userService.register(user2);
        } catch (PersistenceException e) {
            gotException = true;
        }
        assertTrue(gotException);
    }

    @Test
    public void testLoginOk() {
        String login = "testClient1";
        User ret = userService.login(login, login);
        assertNotNull(ret);
        assertEquals(ret.getLogin(), login);
    }

    @Test
    public void testLoginBadPass() {
        User ret = userService.login("testClient1", "bad-password");
        assertNull(ret);
    }

    @Test
    public void testLoginBadUser() {
        User ret = userService.login("testNonexistentUser", "anyPassword");
        assertNull(ret);
    }

    @Test
    public void testLoginBlockedUser() {
        User ret = userService.login("testNonexistentUser", "anyPassword");
        assertNull(ret);
    }

}

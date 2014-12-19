package gl.glue.brahma.test;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import com.typesafe.config.ConfigFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;

import org.junit.BeforeClass;
import play.db.jpa.JPA;
import play.db.jpa.JPAPlugin;
import play.test.FakeApplication;
import play.test.Helpers;
import scala.Option;

import java.util.HashMap;
import java.util.Map;


public abstract class TransactionalTest {

    protected static play.api.test.FakeApplication app;
    protected static EntityManager em;
    protected EntityTransaction tx;

    @BeforeClass
    public static void setUp() {
        Map<String, String> settings = new HashMap<String, String>();
        ConfigFactory.parseResourcesAnySyntax("application-test.conf").entrySet().forEach(
                entry -> settings.put(entry.getKey(), entry.getValue().unwrapped().toString()));
        FakeApplication myapp = Helpers.fakeApplication(settings);
        Helpers.start(myapp);
        app = myapp.getWrappedApplication();
        Option<JPAPlugin> jpaPlugin = app.plugin(JPAPlugin.class);
        em = jpaPlugin.get().em("default");
        JPA.bindForCurrentThread(em);
    }

    @AfterClass
    public static void tearDown() {
        JPA.bindForCurrentThread(null);
        em.close();
    }

    @Before
    public void testStart() {
        tx = em.getTransaction();
        tx.begin();
        tx.setRollbackOnly();
    }

    @After
    public void testEnd() {
        tx.rollback();
    }

}
package utils;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.*;

import play.Configuration;
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
        Config config = ConfigFactory.parseResourcesAnySyntax("application-test.conf").resolve();
        FakeApplication myapp = Helpers.fakeApplication(new Configuration(config).asMap());
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
package gl.glue.brahma.controllers;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import gl.glue.brahma.model.accesslog.AccessLog;
import gl.glue.brahma.model.attachment.Attachment;
import gl.glue.brahma.model.availability.Availability;
import gl.glue.brahma.model.collective.Collective;
import gl.glue.brahma.model.field.Field;
import gl.glue.brahma.model.historyarchive.HistoryArchive;
import gl.glue.brahma.model.historycurrent.HistoryCurrent;
import gl.glue.brahma.model.historyentry.HistoryEntry;
import gl.glue.brahma.model.historyentrytype.HistoryEntryType;
import gl.glue.brahma.model.institution.Institution;
import gl.glue.brahma.model.notification.Notification;
import gl.glue.brahma.model.service.Service;
import gl.glue.brahma.model.servicetype.ServiceType;
import gl.glue.brahma.model.session.Session;
import gl.glue.brahma.model.sessionlog.SessionLog;
import gl.glue.brahma.model.sessionuser.SessionUser;
import gl.glue.brahma.model.transaction.Transaction;
import gl.glue.brahma.model.user.User;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;

public class Application extends Controller {

    @Transactional
    public static Result index() {

        System.out.println("########## Showing all entities ##########");

        System.out.println("\n-- Collective --");
        JPA.em().createQuery("select x from Collective x", Collective.class).getResultList().forEach((i) -> System.out.println(i));

        System.out.println("\n-- Institution --");
        JPA.em().createQuery("select x from Institution x", Institution.class).getResultList().forEach((i) -> System.out.println(i));

        System.out.println("\n-- User --");
        JPA.em().createQuery("select x from User x", User.class).getResultList().forEach((i) -> System.out.println(i));

        System.out.println("\n-- Field --");
        JPA.em().createQuery("select x from Field x", Field.class).getResultList().forEach((i) -> System.out.println(i));

        System.out.println("\n-- ServiceType --");
        JPA.em().createQuery("select x from ServiceType x", ServiceType.class).getResultList().forEach((i) -> System.out.println(i));

        System.out.println("\n-- Service --");
        JPA.em().createQuery("select x from Service x", Service.class).getResultList().forEach((i) -> System.out.println(i));

        System.out.println("\n-- Availability --");
        JPA.em().createQuery("select x from Availability x", Availability.class).getResultList().forEach((i) -> System.out.println(i));

        System.out.println("\n-- Notification --");
        JPA.em().createQuery("select x from Notification x", Notification.class).getResultList().forEach((i) -> System.out.println(i));

        System.out.println("\n-- Session --");
        JPA.em().createQuery("select x from Session x", Session.class).getResultList().forEach((i) -> System.out.println(i));

        System.out.println("\n-- SessionUser --");
        JPA.em().createQuery("select x from SessionUser x", SessionUser.class).getResultList().forEach((i) -> System.out.println(i));

        System.out.println("\n-- SessionLog --");
        JPA.em().createQuery("select x from SessionLog x", SessionLog.class).getResultList().forEach((i) -> System.out.println(i));

        System.out.println("\n-- HistoryCurrent --");
        JPA.em().createQuery("select x from HistoryCurrent x", HistoryCurrent.class).getResultList().forEach((i) -> System.out.println(i));

        System.out.println("\n-- AccessLog --");
        JPA.em().createQuery("select x from AccessLog x", AccessLog.class).getResultList().forEach((i) -> System.out.println(i));

        System.out.println("\n-- HistoryEntryType --");
        JPA.em().createQuery("select x from HistoryEntryType x", HistoryEntryType.class).getResultList().forEach((i) -> System.out.println(i));

        System.out.println("\n-- HistoryEntry --");
        JPA.em().createQuery("select x from HistoryEntry x", HistoryEntry.class).getResultList().forEach((i) -> System.out.println(i));

        System.out.println("\n-- Attachment --");
        JPA.em().createQuery("select x from Attachment x", Attachment.class).getResultList().forEach((i) -> System.out.println(i));

        System.out.println("\n-- Transaction --");
        JPA.em().createQuery("select x from Transaction x", Transaction.class).getResultList().forEach((i) -> System.out.println(i));

        System.out.println("\n-- HistoryArchive --");
        JPA.em().createQuery("select x from HistoryArchive x", HistoryArchive.class).getResultList().forEach((i) -> System.out.println(i));

        System.out.println("\n########## Success ##########");

        return ok(index.render("It works."));
    }

    public static Result preflight(String all) {
        Config conf = ConfigFactory.load();
        response().setHeader("Access-Control-Allow-Origin", conf.getString("cors.origin"));
        response().setHeader("Allow", "*");
        response().setHeader("Access-Control-Allow-Methods", "POST, GET, PUT, DELETE, OPTIONS");
        response().setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, Referer, User-Agent");
        response().setHeader("Access-Control-Allow-Credentials", "true");
        response().setHeader("Access-Control-Max-Age", "86400");
        return ok();
    }
}

package gl.glue.brahma.controllers;

import gl.glue.brahma.model.availability.Availability;
import gl.glue.brahma.model.collective.Collective;
import gl.glue.brahma.model.field.Field;
import gl.glue.brahma.model.institution.Institution;
import gl.glue.brahma.model.service.Service;
import gl.glue.brahma.model.servicetype.ServiceType;
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
        JPA.em().createQuery("select x from Institution x", Institution.class).getResultList().forEach((i)-> System.out.println(i));

        System.out.println("\n-- User --");
        JPA.em().createQuery("select x from User x", User.class).getResultList().forEach((i)-> System.out.println(i));

        System.out.println("\n-- Field --");
        JPA.em().createQuery("select x from Field x", Field.class).getResultList().forEach((i)-> System.out.println(i));

        System.out.println("\n-- ServiceType --");
        JPA.em().createQuery("select x from ServiceType x", ServiceType.class).getResultList().forEach((i)-> System.out.println(i));

        System.out.println("\n-- Service --");
        JPA.em().createQuery("select x from Service x", Service.class).getResultList().forEach((i)-> System.out.println(i));

        System.out.println("\n-- Availability --");
        JPA.em().createQuery("select x from Availability x", Availability.class).getResultList().forEach((i)-> System.out.println(i));

        System.out.println("\n########## Success ##########");

        return ok(index.render("It works."));
    }

}

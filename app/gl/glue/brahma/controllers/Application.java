package gl.glue.brahma.controllers;

import gl.glue.brahma.model.colective.Colective;
import gl.glue.brahma.model.user.Client;
import play.db.jpa.JPA;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;

import java.util.Arrays;
import java.util.List;

public class Application extends Controller {

    @play.db.jpa.Transactional
    public static Result index() {

        Client client = JPA.em().createQuery("select c from Client c", Client.class).getSingleResult();
        String message = client.toString();
        message += " - tutorized by: " + client.getTutor();

        return ok(index.render(message));
    }

}

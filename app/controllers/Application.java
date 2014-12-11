package controllers;

import model.Colective;
import play.db.jpa.JPA;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;

import java.util.Arrays;
import java.util.List;

public class Application extends Controller {

    @play.db.jpa.Transactional
    public static Result index() {

        /*
        Colective col = new Colective();
        col.setName("Clientes Mutua Madrileña");
        JPA.em().persist(col);
        */
        List<Colective> colectives = JPA.em().createQuery("select c from Colective c", Colective.class).getResultList();
        String message = Arrays.toString(colectives.toArray());

        return ok(index.render(message));
    }

}

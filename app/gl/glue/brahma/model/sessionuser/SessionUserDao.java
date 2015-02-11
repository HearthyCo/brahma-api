package gl.glue.brahma.model.sessionuser;


import play.db.jpa.JPA;

public class SessionUserDao {

    public void create(SessionUser sessionUser) {
        JPA.em().persist(sessionUser);
    }
}

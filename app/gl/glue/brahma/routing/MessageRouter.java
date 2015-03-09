package gl.glue.brahma.routing;

import gl.glue.play.amqp.Router;
import gl.glue.brahma.controllers.amq.MessageController;

public class MessageRouter {

    static {
        Router.on("echo").routeTo(MessageController::echoBack);
        Router.on("#").routeTo(MessageController::doNothing);
    }

}
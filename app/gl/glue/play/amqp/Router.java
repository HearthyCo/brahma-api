package gl.glue.play.amqp;

import com.rabbitmq.client.QueueingConsumer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;


/**
 * Sends each message received to the corresponding controller.
 * <p>
 * This class is usually called by {@link Plugin}, but can be used standalone.
 */
public class Router {

    /**
     * An entry on the routing table.
     */
    public static class Route {
        private Pattern pattern;
        private Consumer<QueueingConsumer.Delivery> action = null;

        /**
         * Creates a new route for the specified pattern.
          * @param pattern A string with the pattern specification. Can contain "*" and "#" wildcards,
         *                 as defined on the AMQP spec.
         */
        public Route(String pattern) {
            String realPattern = "";
            for (String s: pattern.split("\\.")) {
                if (s.equals("*")) realPattern += "[^.]*";
                else if (s.equals("#")) realPattern += ".*";
                else realPattern += Pattern.quote(s);
            }
            this.pattern = Pattern.compile(realPattern);
        }

        /**
         * Sets the controller for this route.
         * @param action The controller to handle this route.
         */
        public void routeTo(Consumer<QueueingConsumer.Delivery> action) {
            this.action = action;
        }

        /**
         * Checks if a given routing key matches this route.
         * @param value The routing key to check.
         * @return true if there is a match, false otherwise.
         */
        private boolean matches(String value) {
            return pattern.matcher(value).matches();
        }

        /**
         * Calls the controller associated with this route.
         * @param value The Delivery being routed.
         */
        private void call(QueueingConsumer.Delivery value) {
            action.accept(value);
        }
    }

    private static List<Route> routes = new ArrayList<>();

    /**
     * Calls the controller corresponding to the delivery's routing key.
     * <p>
     * Each route on the routing table will be checked until finding a match.
     * Once found, its associated controller will be called with the delivery as the argument.
     * @param delivery The message to route.
     */
    public static void route(QueueingConsumer.Delivery delivery) {
        String routingKey = delivery.getEnvelope().getRoutingKey();
        for (Route route: routes) {
            if (route.matches(routingKey)) {
                route.call(delivery);
                return;
            }
        }
    }

    /**
     * Adds a new route to the routing table.
     * <p>
     * A call to {@link gl.glue.play.amqp.Router.Route#routeTo} should be chained after this.
     *
     * @param pattern The pattern for the route.
     * @return The new {@link gl.glue.play.amqp.Router.Route}.
     */
    public static Route on(String pattern) {
        Route route = new Route(pattern);
        routes.add(route);
        return route;
    }

    /**
     * Clears the routing table.
     */
    public static void clear() {
        routes.clear();
    }

}

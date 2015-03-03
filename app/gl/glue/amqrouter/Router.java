package gl.glue.amqrouter;

import com.rabbitmq.client.QueueingConsumer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;


public class Router {

    public static class Route {
        private Pattern pattern;
        private Consumer<QueueingConsumer.Delivery> action = null;
        public Route(String pattern) {
            String realPattern = "";
            for (String s: pattern.split("\\.")) {
                if (s.equals("*")) realPattern += "[^.]*";
                else if (s.equals("#")) realPattern += ".*";
                else realPattern += Pattern.quote(s);
            }
            this.pattern = Pattern.compile(realPattern);
        }
        public void routeTo(Consumer<QueueingConsumer.Delivery> action) {
            this.action = action;
        }
        private boolean matches(String value) {
            return pattern.matcher(value).matches();
        }
        private void call(QueueingConsumer.Delivery value) {
            action.accept(value);
        }
    }

    private static List<Route> routes = new ArrayList<>();


    public static void route(QueueingConsumer.Delivery delivery) {
        String routingKey = delivery.getEnvelope().getRoutingKey();
        for (Route route: routes) {
            if (route.matches(routingKey)) {
                route.call(delivery);
                return;
            }
        }
    }

    public static Route on(String pattern) {
        Route route = new Route(pattern);
        routes.add(route);
        return route;
    }

    public static void clear() {
        routes.clear();
    }

}

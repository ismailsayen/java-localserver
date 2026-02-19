package config.model;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import server.Route;
import server.Server;

public class WebServerConfig {
    private final LinkedHashSet<Object> servers;

    public WebServerConfig(LinkedHashSet<Object> servers) {
        this.servers = servers;
    }

    // [{path=/, methods=[GET, POST]}], port=8080, host=127.0.0.1, name=server1,
    // default_server=true}]
    public List<Server> setup() {

        List<Server> serversList = new ArrayList<>();
        for (Object elem : this.servers) {
            Map<String, Object> ser = (Map<String, Object>) elem;
            String host = (String) ser.get("host");
            String name = (String) ser.get("name");
            Object portObj = ser.get("port");
            Integer port = null;

            if (portObj instanceof Integer) {
                port = (Integer) portObj;
            } else if (portObj instanceof Double aDouble) {
                port = aDouble.intValue();
            } else if (portObj instanceof String string) {
                port = Integer.valueOf(string);
            } else {
                throw new RuntimeException("Invalid port type: " + portObj);
            }

            LinkedHashSet<Object> routesRaw = (LinkedHashSet<Object>) ser.get("routes");
            List<Route> routes = extractRoutes(routesRaw);

            // defaultServer n'existe pas dans ton JSON
            Server server = new Server(host, port, null, name, routes);

            serversList.add(server);
        }
        return serversList;
    }

    private List<Route> extractRoutes(LinkedHashSet<Object> routes) {

        List<Route> routeList = new ArrayList<>();

        for (Object elem : routes) {

            Map<String, Object> rt = (Map<String, Object>) elem;

            String path = (String) rt.get("path");

            LinkedHashSet<Object> methodsRaw = (LinkedHashSet<Object>) rt.get("methods");

            List<String> methods = new ArrayList<>();
            for (Object method : methodsRaw) {
                methods.add((String) method);
            }

            Route route = new Route(path, methods);
            routeList.add(route);
        }

        return routeList;
    }

}

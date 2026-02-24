package config.model;

import config.utils.Validators;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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
            Server server = Validators.ServerValidator(ser);
            if (server == null) {
                System.out.println("khritiha hna =>\n" + ser);
                continue;
            }

            String name = (String) ser.get("name");
            int counter = 1;

            for (Server s : serversList) {
                if (s.getName().trim().equals(name.trim())) {
                    counter++;
                    name = String.format("%s_%d", name, counter);
                    break;
                }
            }

            server.setName(name);
            serversList.add(server);
        }
        return serversList;
    }

    // private List<Route> extractRoutes(LinkedHashSet<Object> routes) {

    // List<Route> routeList = new ArrayList<>();

    // for (Object elem : routes) {

    // Map<String, Object> rt = (Map<String, Object>) elem;

    // String path = (String) rt.get("path");

    // LinkedHashSet<Object> methodsRaw = (LinkedHashSet<Object>) rt.get("methods");

    // List<String> methods = new ArrayList<>();
    // for (Object method : methodsRaw) {
    // methods.add((String) method);
    // }

    // // Route route = new Route(path, methods);
    // // routeList.add(route);
    // }
    // return routeList;
    // }

    // private List<Route> extractCustomRoutes(LinkedHashSet<Object> routes) {

    // List<Route> routeList = new ArrayList<>();

    // return routeList;
    // }
}

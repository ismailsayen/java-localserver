package config.model;

import config.utils.Validators;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import DTO.Route;
import DTO.Server;

public class WebServerConfig {
    private final LinkedHashSet<Object> servers;

    public WebServerConfig(LinkedHashSet<Object> servers) {
        this.servers = servers;
    }

    public List<Server> setup() {

        List<Server> serversList = new ArrayList<>();
        for (Object elem : this.servers) {
            Map<String, Object> ser = (Map<String, Object>) elem;

            Server server = Validators.ServerValidator(ser);

            if (server == null) {
                System.out.println("khritiha hna =>\n" + ser);
                continue;
            }

            String name = (String) ser.getOrDefault("name", "default");
            int counter = 1;

            for (Server s : serversList) {
                if (s.getName().trim().equals(name.trim())) {
                    name = String.format("%s_%d", name.trim(), counter);
                    counter++;
                }
            }

            server.setName(name);

            LinkedHashSet<Object> routes = (LinkedHashSet<Object>) ser.get("routes");
            List<Route> extractedRoutes = extractRoutes(routes);
            server.setRoutes(extractedRoutes);
            serversList.add(server);
        }
        return serversList;
    }

    private List<Route> extractRoutes(LinkedHashSet<Object> routes) {

        List<Route> routeList = new ArrayList<>();

        for (Object elem : routes) {

            Map<String, Object> rt = (Map<String, Object>) elem;

            Route route = Validators.routeValidator(rt);
            if (route == null) {
                System.out.println("khritiha f had route =>\n" + route);
                continue;
            }
            routeList.add(route);

        }
        return routeList;
    }

}

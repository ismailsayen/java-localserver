package config.model;

import DTO.Route;
import DTO.Server;
import config.utils.Validators;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

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
                System.err.println("[ERROR] Invalid server configuration: " );
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
            if (extractedRoutes==null){
                System.err.println("[ERROR] Invalid server configuration: " );
                continue;
            }
            server.setRoutes(extractedRoutes);
            serversList.add(server);
        }
        return serversList;
    }

    private List<Route> extractRoutes(LinkedHashSet<Object> routes) {

        List<Route> routeList = new ArrayList<>();

        if(routes==null){
            return null;
        }

        for (Object elem : routes) {

            Map<String, Object> rt = (Map<String, Object>) elem;

            Route route = Validators.routeValidator(rt);
            if (route == null) {
                System.err.println("[ERROR] Invalid route configuration: ");
                continue;
            }
            routeList.add(route);
        }
        return routeList;
    }

}

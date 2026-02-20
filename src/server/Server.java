package server;

import java.util.LinkedHashSet;
import java.util.List;

public class Server {
    private String host;
    private LinkedHashSet<Object> port;
    private String defaultServer;
    private String name;
    private List<Route> routes;
    private Long limitRequestBody;

    public Server(String host, LinkedHashSet<Object> port, String defaultServer, String name, List<Route> routes,
            Long limitRequestBody) {
        this.host = host;
        this.port = port;
        this.defaultServer = defaultServer;
        this.name = name;
        this.routes = routes;
        this.limitRequestBody = limitRequestBody;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public LinkedHashSet<Object> getPort() {
        return port;
    }

    public void setPort(LinkedHashSet<Object> port) {
        this.port = port;
    }

    public String getDefaultServer() {
        return defaultServer;
    }

    public void setDefaultServer(String defaultServer) {
        this.defaultServer = defaultServer;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getLimitRequestBody() {
        return this.limitRequestBody;
    }

    public void setLimitRequestBody(Long limitRequestBody) {
        this.limitRequestBody = limitRequestBody;
    }

    public List<Route> getRoutes() {
        return routes;
    }

    public void setRoutes(List<Route> routes) {
        this.routes = routes;
    }

}

package server;

import java.util.List;

public class Server {
    private String host;
    private Integer port;
    private String defaultServer;
    private String name;
    private List<Route> routes;

    public Server(String host, Integer port, String defaultServer, String name, List<Route> routes) {
        this.host = host;
        this.port = port;
        this.defaultServer = defaultServer;
        this.name = name;
        this.routes = routes;
    }

    
    public String getHost() {
        
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

   
    public void setPort(Integer port) {
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


    public List<Route> getRoutes() {
        return routes;
    }

   
    public void setRoutes(List<Route> routes) {
        this.routes = routes;
    }

}

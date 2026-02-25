package server;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public class Server {
    private String host;
    private LinkedHashSet<Object> port;
    private Boolean defaultServer;
    private String name;
    private List<Route> routes;
    private Long limitRequestBody;
    private Map<String,String> errorPages;


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

    public Boolean getDefaultServer() {
        return defaultServer;
    }

    public void setDefaultServer(Boolean defaultServer) {
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

    public Map<String,String> getErrorPages() {
        return errorPages;
    }

    public void setErrorPages(Map<String,String> errorPages) {
        this.errorPages = errorPages;
    }

    @Override
public String toString() {
    return """
           Server {
             name='""" + name + '\'' + ",\n" +
            "  host='" + host + '\'' + ",\n" +
            "  ports=" + port + ",\n" +
            "  defaultServer=" + defaultServer + ",\n" +
            "  limitRequestBody=" + limitRequestBody + ",\n" +
            "  routes=" + routes + ",\n" +
            "  errorPages=" + errorPages + "\n" +
            '}';
}

}

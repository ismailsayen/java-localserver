package server;

import java.util.List;

public class Route {
    private String path;
    private List<String> methods;

    public Route(String path, List<String> methods) {
        this.path = path;
        this.methods = methods;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<String> getMethods() {
        return methods;
    }

    public void setMethods(List<String> methods) {
        this.methods = methods;
    }
}

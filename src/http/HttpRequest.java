package http;

import DTO.Route;
import DTO.Server;
import Nio.ClientHandler;
import handlers.CgiHandler;
import handlers.DeleteHandler;
import handlers.MultipartHandler;
import handlers.RedirectHandler;
import handlers.StaticFileHandler;
import java.io.IOException;

public class HttpRequest {

    private final HttpHeader httpHeader;
    private RequestStatus status = RequestStatus.READY;
    private HttpHandler hnadler;
    private Server server;
    private Route route;
    private byte[] body;

    public HttpRequest(HttpHeader httpHeader, Server server) {
        this.httpHeader = httpHeader;
        this.server = server;
    }

    public void HandleRequest(ClientHandler client) {
        this.route = this.extractRoute();
        if (status != RequestStatus.READY)
            return;

        if (route.getRedirectTo() != null) {
            this.setHnadler(new RedirectHandler(client));
            return;
        }

        assignHandler(client);
    }

    private Route extractRoute() {
        String method = httpHeader.getMethod().toUpperCase();
        String requestPath = httpHeader.getPath();

        Route bestMatch = null;
        int longestMatch = -1;

        for (Route r : server.getRoutes()) {

            String routePath = r.getPath();

            if (!requestPath.startsWith(routePath))
                continue;

            if (routePath.length() > longestMatch) {
                bestMatch = r;
                longestMatch = routePath.length();
            }
        }

        if (bestMatch == null) {
            status = RequestStatus.NOT_FOUND;
            throw new RuntimeException("Route not found");
        }

        if (!bestMatch.getMethods().contains(method)) {
            status = RequestStatus.METHOD_NOT_ALLOWED;
            throw new RuntimeException("Method not allowed");
        }

        return bestMatch;

    }

    private void assignHandler(ClientHandler client) {
        String path = httpHeader.getPath();
        String method = httpHeader.getMethod().toUpperCase();
        String contentType = this.httpHeader.getHeaders().get("content-type");

        if (path.contains("/cgi-bin/") || path.endsWith(".py") || path.endsWith(".php")) {
            this.setHnadler(new CgiHandler(client));
            return;
        }

        // 2. Détection Multipart
        if (contentType != null && contentType.contains("multipart/form-data")) {
            this.setHnadler(new MultipartHandler(client));
            return;
        }

        if (method.equals("DELETE")) {
            this.setHnadler(new DeleteHandler(client));
            return;
        }

        // 3. Cas Statique (GET / DELETE)
        if (method.equals("GET")) {
            this.setHnadler(new StaticFileHandler(client));
            return;
        }

        this.setHnadler(new StaticFileHandler(client));
    }

    public void executeHandler(ClientHandler client) throws Exception {
        if (hnadler == null)
            throw new RuntimeException("No handler assigned");

        hnadler.handle();
    }

    public void executeResponse(ClientHandler client) throws IOException {
        if (hnadler == null)
            throw new RuntimeException("No handler assigned");

        hnadler.response();
    }

    public String resolveFilePath() {
        String requestPath = httpHeader.getPath();
        String routePath = route.getPath();
        String root = route.getRoot();
        String relativePath = requestPath.substring(routePath.length());
        if (relativePath.isEmpty() || relativePath.equals("/")) {
            if (route.getIndex() != null)
                return root + "/" + route.getIndex();
        }

        return root + relativePath;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public HttpHandler getHnadler() {
        return hnadler;
    }

    public void setHnadler(HttpHandler hnadler) {
        this.hnadler = hnadler;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    public byte[] getBody() {
        return this.body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }
}
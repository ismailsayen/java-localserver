package http;

import DTO.Route;
import DTO.Server;
import Nio.ClientHandler;
import handlers.CgiHandler;
import handlers.DeleteHandler;
import handlers.MultipartHandler;
import handlers.RedirectHandler;
import handlers.StaticFileHandler;

public class HttpRequest {

    private HttpHeader httpHeader;
    private RequestStatus status = RequestStatus.READY;

    private Long contentLength = 0L;
    private boolean chnked = false;

    private Boolean isMultipart = false;
    private Boolean isRedirectPath = false;
    private String boundary;
    private HttpHandler hnadler;
    private Server server;
    private Route route;
    private byte[] body;

    public HttpRequest(HttpHeader httpHeader, Server server) {
        this.httpHeader = httpHeader;
        this.server = server;
    }

    public void HandleRequest() {
        this.route = this.extractRoute();
        if (status != RequestStatus.READY)
            return;

        if (route.getRedirectTo() != null) {
            this.setHnadler(new RedirectHandler());
            return;
        }

        analyzeBody();
        assignHandler();
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

    private void analyzeBody() {
        String cl = httpHeader.getHeaders().get("content-length");
        String te = httpHeader.getHeaders().get("transfer-encoding");

        if (cl == null && te == null) {
            if (httpHeader.getMethod().equalsIgnoreCase("POST"))
                status = RequestStatus.ERROR;

            return;
        } else if (cl != null) {
            this.contentLength = Long.valueOf(cl);
            this.status = (this.contentLength == 0) ? RequestStatus.READY : RequestStatus.PROCESSING;
        } else {
            this.chnked = true;
            this.status = RequestStatus.PROCESSING;
        }
        extractMultipartDetails();
    }

    private void extractMultipartDetails() {
        String contentType = httpHeader.getHeaders().get("content-type");

        if (contentType == null)
            return;

        if (contentType.contains("multipart/form-data")) {

            isMultipart = true;

            String[] parts = contentType.split("boundary=");

            if (parts.length > 1)
                boundary = parts[1];
        }

    }

    private void assignHandler() {
        String method = httpHeader.getMethod().toUpperCase();

        if (route.getCgi() != null) {
            this.setHnadler(new CgiHandler());
            return;
        }

        if (this.isMultipart) {
            this.setHnadler(new MultipartHandler());
            return;
        }

        if (method.equals("DELETE")) {
            this.setHnadler(new DeleteHandler());
            return;
        }

        if (method.equals("GET")) {
            this.setHnadler(new StaticFileHandler());
            return;
        }

        this.setHnadler(new StaticFileHandler());
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

    public void executeHandler(ClientHandler client) throws Exception {
        if (hnadler == null)
            throw new RuntimeException("No handler assigned");

        hnadler.handle(this, client);
    }

    public Long getContentLength() {
        return contentLength;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public boolean isChnked() {
        return chnked;
    }

    public void setChnked(boolean chnked) {
        this.chnked = chnked;
    }

    public Boolean getIsMultipart() {
        return isMultipart;
    }

    public void setIsMultipart(Boolean isMultipart) {
        this.isMultipart = isMultipart;
    }

    public String getBoundary() {
        return this.boundary;
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

    public Boolean getIsRedirectPath() {
        return isRedirectPath;
    }

    public void setIsRedirectPath(Boolean isRedirectPath) {
        this.isRedirectPath = isRedirectPath;
    }

    public HttpHeader getHttpHeader() {
        return httpHeader;
    }

    public void setHttpHeader(HttpHeader httpHeader) {
        this.httpHeader = httpHeader;
    }

    public byte[] getBody() {
        return this.body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }
}
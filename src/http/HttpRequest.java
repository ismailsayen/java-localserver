package http;

import DTO.Route;
import DTO.Server;
import handlers.CgiHandler;
import handlers.DeleteHandler;
import handlers.MultipartHandler;
import handlers.StaticFileHandler;

public class HttpRequest {

    private HttpHeader httpHeader;
    private RequestStatus status = RequestStatus.READY;
    private Long contentLength = 0L;
    private boolean chnked = false;
    private Boolean isMultipart = false;
    private String boundary;
    private HttpHandler hnadler;
    private Server server;
    private Route route;

    public HttpRequest(HttpHeader httpHeader, Server server) {
        this.httpHeader = httpHeader;
        this.server = server;
    }

    public void HandleRequest() {
        Route route = this.extractRoute();
        validatePayloadMethod();
    }

    private Route extractRoute() {
        String method = this.httpHeader.getMethod().toUpperCase();
        String path = this.httpHeader.getPath();

        for (Route route : this.server.getRoutes()) {
            if (!route.getPath().equals(path))
                continue;

            if (!route.getMethods().contains(method)) {
                this.status = RequestStatus.METHOD_NOT_ALLOWED;
                throw new RuntimeException("Method not allowed");
            }

            return route;
        }

        this.status = RequestStatus.NOT_FOUND;
        throw new RuntimeException("Resources not found");
    }

    private void validatePayloadMethod() {
        String cl = httpHeader.getHeaders().get("content-length");
        String te = httpHeader.getHeaders().get("transfer-encoding");

        if (cl == null && te == null) {

            this.status = (httpHeader.getMethod().toUpperCase().equals("POST")) ? RequestStatus.ERROR
                    : RequestStatus.READY;
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
        String contentType = this.httpHeader.getHeaders().get("content-type");
        if (contentType != null && contentType.contains("multipart/form-data")) {
            if (contentType.contains("boundary=")) {
                this.isMultipart = true;
                String[] parts = contentType.split("boundary=");
                if (parts.length > 1) {
                    this.boundary = parts[1].trim();
                    if (this.boundary.startsWith("\"") && this.boundary.endsWith("\"")) {
                        this.boundary = this.boundary.substring(1, this.boundary.length() - 1);
                    }
                }
            }
        }
    }

    private void assignHandler() {
        String path = httpHeader.getPath();
        String method = httpHeader.getMethod().toUpperCase();
        if (path.contains("/cgi-bin/") || path.endsWith(".py") || path.endsWith(".php")) {
            this.setHnadler(new CgiHandler());
            return;
        }

        // 2. Détection Multipart
        if (this.isMultipart) {
            this.setHnadler(new MultipartHandler());
            return;
        }

        if (method.equals("DELETE")) {
            this.setHnadler(new DeleteHandler());
            return;
        }

        // 3. Cas Statique (GET / DELETE)
        if (method.equals("GET")) {
            this.setHnadler(new StaticFileHandler());
            return;
        }

        this.setHnadler(new StaticFileHandler());
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
}
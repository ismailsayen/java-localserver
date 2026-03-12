package http;

import Nio.ClientHandler;

public interface HttpHandler {
    public void read();

    public void write();

    void handle(HttpRequest request, ClientHandler client) throws Exception;
}

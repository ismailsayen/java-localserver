package http;

import java.io.IOException;

import Nio.ClientHandler;

public interface HttpHandler {

    public void sendResponse(int status, String contentType, byte[] responseBody)  throws IOException;

    void handle(HttpRequest request, ClientHandler client) throws Exception;
}

package handlers;

import java.io.File;
import java.io.IOException;

import Nio.ClientHandler;
import http.HttpHandler;
import http.HttpRequest;

public class CgiHandler implements HttpHandler {

    private final ClientHandler client;
    private byte[] responseBytes;

    public CgiHandler(ClientHandler client) {
        this.client = client;
    }

    @Override
    public void handle() throws Exception {
        HttpRequest request = client.getHttpRequest();

        String method = client.getHttpHeader().getMethod();
        String scriptPath = request.resolveFilePath();

        File script = new File(scriptPath);
        if (!script.exists()) {
            responseBytes = "<h1>404 Not Found</h1>".getBytes();
            return;
        }
    }

    @Override
    public void response() throws IOException {
        
    }

}

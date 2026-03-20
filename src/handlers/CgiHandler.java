package handlers;

import java.io.IOException;

import Nio.ClientHandler;
import http.HttpHandler;

public class CgiHandler implements HttpHandler {

    private final ClientHandler client;
    private byte[] responseBytes;

    public CgiHandler(ClientHandler client) {
        this.client = client;
    }

    @Override
    public void handle() throws Exception {
        
    }

    @Override
    public void response() throws IOException {
        
    }

}

package handlers;

import java.io.IOException;

import Nio.ClientHandler;
import http.HttpHandler;

public class DeleteHandler implements HttpHandler {

    private final ClientHandler client;

    public DeleteHandler(ClientHandler client) {
        this.client = client;
    }

    @Override
    public void handle() throws Exception {
       System.out.println("zzzz");
    }
     @Override
    public void response() throws IOException {
    }


}

package handlers;

import java.io.IOException;

import Nio.ClientHandler;
import http.HttpHandler;

public class RedirectHandler implements HttpHandler {

    private final ClientHandler client;

    public RedirectHandler(ClientHandler client) {
        this.client = client;
    }

    @Override
    public void handle() throws Exception {
        // TODO Auto-generated method stub
        System.out.println(this.client);
        throw new UnsupportedOperationException("Unimplemented method 'handle'");
    }

    @Override
    public void response() throws IOException {
        // TODO Auto-generated method stub
    }

}

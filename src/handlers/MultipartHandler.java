package handlers;

import Nio.ClientHandler;
import http.HttpHandler;

public class MultipartHandler implements HttpHandler {

    private final ClientHandler client;

    public MultipartHandler(ClientHandler client) {
        this.client = client;
    }

    @Override
    public void handle() throws Exception {
        // TODO Auto-generated method stub
        System.out.println(this.client);
        throw new UnsupportedOperationException("Unimplemented method 'handle'");
    }

}

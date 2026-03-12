package handlers;

import Nio.ClientHandler;
import http.HttpHandler;
import http.HttpRequest;

public class DeleteHandler implements HttpHandler {
    
    @Override
    public void read() {
        System.out.println("Not supported yet. delete");
    }

    @Override
    public void write() {
     System.out.println("Not supported yet. delete");
    }

    
    @Override
    public void handle(HttpRequest request, ClientHandler client) throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'handle'");
    }
    
}

package handlers;

import Nio.ClientHandler;
import http.HttpHandler;
import http.HttpRequest;

public class MultipartHandler implements HttpHandler {

    @Override
    public void read() {
        // TODO Auto-generated method stub
       System.out.println("Unimplemented method 'read' MultipartHandler");
    }

    @Override
    public void write() {
        // TODO Auto-generated method stub
       System.out.println("Unimplemented method 'write' MultipartHandler");
    }


    @Override
    public void handle(HttpRequest request, ClientHandler client) throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'handle'");
    }
    
}

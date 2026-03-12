package handlers;

import Nio.ClientHandler;
import http.HttpHandler;
import http.HttpRequest;

public class CgiHandler implements HttpHandler {

    @Override
    public void read() {
        // TODO Auto-generated method stub
        System.out.println("Unimplemented method 'read' cgi");
    }

    @Override
    public void write() {
        // TODO Auto-generated method stub
        System.out.println("Unimplemented method 'write' cgi");
    }


    @Override
    public void handle(HttpRequest request, ClientHandler client) throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'handle'");
    }

}

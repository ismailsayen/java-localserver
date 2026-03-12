package handlers;

import Nio.ClientHandler;
import http.HttpHandler;
import http.HttpRequest;

public class ChunkedHundler implements HttpHandler {

    @Override
    public void read() {
        // TODO Auto-generated method stub
        System.out.println("Unimplemented method 'read' chunked");
    }

    @Override
    public void write() {
        // TODO Auto-generated method stub
        System.out.println("Unimplemented method 'write'chunked");
    }

    @Override
    public void handle(HttpRequest request, ClientHandler client) throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'handle'");
    }


    
}

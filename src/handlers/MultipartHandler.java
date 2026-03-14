package handlers;

import java.io.IOException;

import Nio.ClientHandler;
import http.HttpHandler;
import http.HttpRequest;

public class MultipartHandler implements HttpHandler {

   

    @Override
    public void handle(HttpRequest request, ClientHandler client) throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'handle'");
    }

    @Override
    public void sendResponse(int status, String contentType, byte[] responseBody) throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'sendResponse'");
    }

}

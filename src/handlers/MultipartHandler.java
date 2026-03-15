package handlers;

import java.io.IOException;

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
        System.out.println(this.client.getHttpHeader().getHeaders().get("content-type"));
        throw new UnsupportedOperationException("Unimplemented method 'handle'");
    }

    @Override
    public void response() throws IOException {
        // TODO Auto-generated method stub
    }

}

// private void extractMultipartDetails() {

// if () {
// if (contentType.contains("boundary=")) {
// this.isMultipart = true;
// String[] parts = contentType.split("boundary=");
// if (parts.length > 1) {
// this.boundary = parts[1].trim();
// if (this.boundary.startsWith("\"") && this.boundary.endsWith("\"")) {
// this.boundary = this.boundary.substring(1, this.boundary.length() - 1);
// }
// }
// }
// }
// }
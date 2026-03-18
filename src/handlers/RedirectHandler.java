package handlers;

import Nio.ClientHandler;
import http.HttpHandler;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

public class RedirectHandler implements HttpHandler {

    private final ClientHandler client;
    private String redirectTo; // redirection target
    private Long redirectStatusCode;

    public RedirectHandler(ClientHandler client, String redirectTo, Long redirectStatusCode) {
        this.client = client;
        this.redirectTo=redirectTo;
        this.redirectStatusCode=redirectStatusCode;
    }

    @Override
    public void handle() throws Exception {
        client.getKey().interestOps(SelectionKey.OP_WRITE);
    }

    @Override
    public void response() throws IOException {

        

        String statusText;
        switch (redirectStatusCode.intValue()) {
            case 301: statusText = "Moved Permanently"; break;
            case 302: statusText = "Found"; break;
            case 307: statusText = "Temporary Redirect"; break;
            case 308: statusText = "Permanent Redirect"; break;
            default: statusText = "Found";
        }

        String response =
                "HTTP/1.1 " + redirectStatusCode + " " + statusText + "\r\n" +
                "Location: " + this.redirectTo + "\r\n" +
                "Content-Length: 0\r\n" +
                "Connection: close\r\n" +
                "\r\n";

        ByteBuffer buffer = ByteBuffer.wrap(response.getBytes());

        client.getClient().write(buffer);
    }

}

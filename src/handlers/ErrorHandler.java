package handlers;

import Nio.ClientHandler;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class ErrorHandler {

    private ClientHandler client;
    private ByteBuffer buffer;

    public ErrorHandler(ClientHandler client) {
        this.client = client;
    }

    public void error(String statusCode, String msg) {
        Map<String, String> errorPages = this.client.getVirtualHosts().getErrorPages();

        String code = String.valueOf(statusCode);

        String body;

        try {
            if (errorPages != null && errorPages.containsKey(code)) {
                Path file = Path.of(errorPages.get(code));
                body = Files.readString(file);
            } else {
                // fallback if no custom error page
                body = "<h1>" + statusCode + "</h1><p>" + msg + "</p>";
            }
        } catch (IOException e) {
            body = "<h1>" + statusCode + "</h1><p>" + msg + "</p>";
        }

        sendResponse(String.valueOf(statusCode), "text/html", body);
        client.setIsResponseDone(true);
    }

    public void sendResponse(String code, String contentType, String body) {
        String header = "HTTP/1.1 " + code + " \r\n" +
                "Content-Type: " + contentType + "\r\n" +
                "Content-Length: " + body.getBytes().length + "\r\n" +
                "\r\n";

        byte[] responseBytes = (header + body).getBytes();
        this.buffer = ByteBuffer.wrap(responseBytes);

        try {
            while (buffer.hasRemaining()) {
                client.getClient().write(buffer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
package handlers;

import DTO.Route;
import Nio.ClientHandler;
import http.HttpHandler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

public class StaticFileHandler implements HttpHandler {

    private ClientHandler client;

    public StaticFileHandler(ClientHandler client) {
        this.client = client;
    }

    @Override
    public void handle() throws Exception {
        String filePath = this.client.getHttpRequest().resolveFilePath();

        Path file = Path.of(filePath);
        if (!Files.exists(file)) {
            throw new Exception("no Path:" + filePath);
        }
        if (Files.isDirectory(file)) {

            Route rt = this.client.getHttpRequest().getRoute();
            System.out.println(rt);
            if (rt.getIndex() != null) {
                Path indexFile = file.resolve(rt.getIndex());

                if (Files.exists(indexFile)) {
                    byte[] body = Files.readAllBytes(indexFile);
                    sendResponse(200, "text/html", body);
                    return;
                }
            }

            if (rt.getDirectoryListing()) {
                String html = generateDirectoryListing(file.toFile(), rt.getPath());
                sendResponse(200, "text/html", html.getBytes());
                return;
            }

            throw new Exception("Directory listing forbidden");

        }

        byte[] body = Files.readAllBytes(file);

        String contentType = Files.probeContentType(file);
        if (contentType == null)
            contentType = "application/octet-stream";

        sendResponse(200, contentType, body);
    }

    private String generateDirectoryListing(java.io.File directory, String requestPath) {
        StringBuilder html = new StringBuilder();

        html.append("<html><body>");
        html.append("<h1>Index of ").append(requestPath).append("</h1>");

        java.io.File[] files = directory.listFiles();

        if (files != null) {
            for (java.io.File file : files) {

                String name = file.getName();
                String link = requestPath.endsWith("/")
                        ? requestPath + name
                        : requestPath + "/" + name;

                if (file.isDirectory()) {
                    name += "/";
                    link += "/";
                }

                html.append("<a href=\"")
                        .append(link)
                        .append("\">")
                        .append(name)
                        .append("</a><br>");
            }
        }

        html.append("</body></html>");

        return html.toString();
    }

    public void sendResponse(int status, String contentType, byte[] responseBody) throws IOException {

        String statusLine = "HTTP/1.1 " + status + " OK\r\n";

        String headers = "Content-Type: " + contentType + "\r\n" +
                "Content-Length: " + responseBody.length + "\r\n" +
                "Connection: close\r\n\r\n";

        byte[] headerBytes = (statusLine + headers).getBytes();

        ByteBuffer buffer = ByteBuffer.allocate(headerBytes.length + responseBody.length);

        buffer.put(headerBytes);
        buffer.put(responseBody);

        buffer.flip();

        while (buffer.hasRemaining()) {
            this.client.getClient().write(buffer);
        }

        this.client.getClient().close();
    }

}

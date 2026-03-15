package handlers;

import DTO.Route;
import Nio.ClientHandler;
import http.HttpHandler;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.file.Files;
import java.nio.file.Path;

public class StaticFileHandler implements HttpHandler {

    private final ClientHandler client;

    private FileChannel fileChannel;
    private long position = 0;
    private long fileSize = 0;
    private boolean headersSent = false;

    public StaticFileHandler(ClientHandler client) {
        this.client = client;
    }

    @Override
    public void handle() throws Exception {

        
        client.getKey().interestOps(SelectionKey.OP_WRITE);
    }

    @Override
    public void response() throws IOException {

            // send headers first
            if (!headersSent) {
                System.out.println("ssss");

                String filePath = this.client.getHttpRequest().resolveFilePath();
                Path file = Path.of(filePath);

                if (!Files.exists(file)) {
                    throw new IOException("No Path: " + filePath);
                }

                if (Files.isDirectory(file)) {

                    Route rt = this.client.getHttpRequest().getRoute();

                    if (rt.getIndex() != null) {

                        Path indexFile = file.resolve(rt.getIndex());

                        if (Files.exists(indexFile)) {
                            startStreaming(indexFile);
                            return;
                        }
                    }

                    if (rt.getDirectoryListing()) {
                        String html = generateDirectoryListing(file.toFile(), rt);
                        sendResponse(200, "text/html", html.getBytes().length, html.getBytes());
                        return;
                    }

                    throw new IOException("Directory listing forbidden");
                }

                startStreaming(file);
                return;
            }

        // streaming phase
        long chunk = Math.min(1024 * 1024, fileSize - position);

        long transferred = fileChannel.transferTo(position, chunk, client.getClient());

        if (transferred > 0) {
            position += transferred;
        } else {
            return;
        }

        if (position >= fileSize) {

            fileChannel.close();

            SelectionKey key = client.getKey();
            key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);

            client.getClient().close();
        }
    }

    private void startStreaming(Path file) throws IOException {

        String contentType = Files.probeContentType(file);
        if (contentType == null)
            contentType = "application/octet-stream";

        fileSize = Files.size(file);
        fileChannel = FileChannel.open(file);

        headersSent = true;
        sendHeaders(200, contentType, fileSize);

    }

    private String generateDirectoryListing(java.io.File directory, Route rt) {
        StringBuilder html = new StringBuilder();
        String reqPath = rt.getPath().replaceAll("/", "\\\\") + directory.getPath().substring(rt.getRoot().length());
        html.append("<html><body>");
        html.append("<h1>Index of ").append(reqPath).append("</h1>");
        java.io.File[] files = directory.listFiles();
        if (files != null) {
            for (java.io.File file : files) {
                String name = file.getName();
                String link = reqPath + "/" + name;
                if (file.isDirectory()) {
                    name += "/";
                    link += "/";
                }
                html.append("<a href=\"").append(link).append("\">").append(name).append("</a><br>");
            }
        }
        html.append("</body></html>");
        return html.toString();
    }

    public void sendHeaders(int status, String contentType, long contentLength) throws IOException {

        String statusLine = "HTTP/1.1 " + status + " OK\r\n";

        String headers = "Content-Type: " + contentType + "\r\n" +
                "Content-Length: " + contentLength + "\r\n" +
                "Connection: close\r\n\r\n";

        ByteBuffer buffer = ByteBuffer.wrap((statusLine + headers).getBytes());

        while (buffer.hasRemaining()) {
            int written = client.getClient().write(buffer);

            if (written == 0) {
                return;
            }
        }

    }

    public void sendResponse(int status, String contentType, long contentLength, byte[] body)
            throws IOException {

        String statusLine = "HTTP/1.1 " + status + " OK\r\n";

        String headers = "Content-Type: " + contentType + "\r\n" +
                "Content-Length: " + contentLength + "\r\n" +
                "Connection: close\r\n\r\n";

        byte[] headerBytes = (statusLine + headers).getBytes();

        ByteBuffer buffer = ByteBuffer.allocate(headerBytes.length + body.length);

        buffer.put(headerBytes);
        buffer.put(body);
        buffer.flip();

        while (buffer.hasRemaining()) {
            client.getClient().write(buffer);
        }

    }
}

package handlers;

import DTO.Route;
import Nio.ClientHandler;
import config.utils.Session;
import http.HttpHandler;
import java.io.File;
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

    private boolean prepared = false;

    private ByteBuffer headerBuffer;
    private ByteBuffer bodyBuffer;

    public StaticFileHandler(ClientHandler client) {
        this.client = client;
    }

    @Override
    public void handle() {
        client.getKey().interestOps(SelectionKey.OP_WRITE);
    }

    @Override
    public void response() throws IOException {

        if (!prepared) {

            String filePath = client.getHttpRequest().resolveFilePath();
            Path file = Path.of(filePath);
            if (!Files.exists(file)) {
                this.client.getHttpRequest().setStatus("404");
                throw new RuntimeException("Page Not Found");
            }

            if (Files.isDirectory(file)) {

                Route rt = client.getHttpRequest().getRoute();

                if (rt.getIndex() != null) {

                    Path index = file.resolve(rt.getIndex());

                    if (Files.exists(index)) {
                        startFileStreaming(index);
                        prepared = true;
                        return;
                    }
                }

                if (rt.getDirectoryListing()) {
                    sendDirectoryListing(file, rt);
                    prepared = true;
                    return;
                }
                // error 403
                this.client.getHttpRequest().setStatus("403");
                throw new RuntimeException("Access Forbid");               
            }

            startFileStreaming(file);
            prepared = true;
            return;
        }

        // send headers
        if (headerBuffer != null && headerBuffer.hasRemaining()) {

            client.getClient().write(headerBuffer);

            if (headerBuffer.hasRemaining())
                return;

            headerBuffer = null;
        }

        // send directory listing body
        if (bodyBuffer != null && bodyBuffer.hasRemaining()) {

            client.getClient().write(bodyBuffer);

            if (bodyBuffer.hasRemaining())
                return;

            finish();
            return;
        }

        // stream file
        if (fileChannel != null) {

            long chunk = Math.min(64 * 1024, fileSize - position);

            long transferred = fileChannel.transferTo(position, chunk, client.getClient());

            if (transferred > 0)
                position += transferred;

            if (position >= fileSize)
                finish();
        }
    }

    private void startFileStreaming(Path file) throws IOException {

        String contentType = Files.probeContentType(file);

        if (contentType == null)
            contentType = "application/octet-stream";

        fileSize = Files.size(file);
        fileChannel = FileChannel.open(file);

        Session session = client.getHttpRequest().getSession();

        String headers = """
                HTTP/1.1 200 OK\r
                Content-Type: """ + contentType + "\r\n" +
                "Set-Cookie: SESSION_ID=" + session.getId() + "; Path=/\r\n" +
                "Content-Length: " + fileSize + "\r\n" +
                "Connection: close\r\n\r\n";

        headerBuffer = ByteBuffer.wrap(headers.getBytes());
    }

    private void sendDirectoryListing(Path dir, Route rt) {

        String html = generateDirectoryListing(dir.toFile(), rt);

        byte[] body = html.getBytes();

        String headers = """
                HTTP/1.1 200 OK\r
                Content-Type: text/html\r
                Content-Length: """ + body.length + "\r\n" +
                "Connection: close\r\n\r\n";

        headerBuffer = ByteBuffer.wrap(headers.getBytes());
        bodyBuffer = ByteBuffer.wrap(body);
    }

    private void finish() throws IOException {

        if (fileChannel != null)
            fileChannel.close();

        SelectionKey key = client.getKey();

        key.cancel();

        this.client.setIsResponseDone(true);
    }

    private String generateDirectoryListing(File directory, Route rt) {

        StringBuilder html = new StringBuilder();
        String reqPath = directory.getPath().length() > rt.getRoot().length() ? rt.getPath().replaceAll("/", "\\\\") +
                directory.getPath().substring(rt.getRoot().length()) : directory.getPath();

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
}
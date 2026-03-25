package handlers;

import Nio.ClientHandler;
import config.utils.Session;
import http.HttpHandler;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

public class DeleteHandler implements HttpHandler {

    private final ClientHandler client;

    public DeleteHandler(ClientHandler client) {
        this.client = client;
    }

    @Override
    public void handle() throws Exception {
        String filePath = client.getHttpRequest().resolveFilePath();
        Path file = Path.of(filePath);
        if (!Files.exists(file)) {
            this.client.getHttpRequest().setStatus("404");
            throw new RuntimeException("File not found");
        }

        if (Files.isDirectory(file)) {
            this.client.getHttpRequest().setStatus("403");
            throw new RuntimeException("Only files can be deleted");
        }
        try {
            Files.delete(file);
            System.out.println("File deleted successfully");
            Session session = client.getHttpRequest().getSession();

            String response = """
                    HTTP/1.1 200\r
                    Set-Cookie: SESSION_ID=""" + session.getId() + "; Path=/\r\n" +
                    "Content-Length: 0\r\n" +
                    "Connection: close\r\n" +
                    "\r\n";

            ByteBuffer buffer = ByteBuffer.wrap(response.getBytes());

            client.getClient().write(buffer);
            client.getClient().close();
        } catch (IOException e) {

            client.getHttpRequest().setStatus("500");
            throw new RuntimeException("Can't delete file", e);
        }
    }

    @Override
    public void response() throws IOException {
    }

}

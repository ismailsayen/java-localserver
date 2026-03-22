package handlers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.Map;

import Nio.ClientHandler;
import config.utils.Session;
import http.HttpHandler;
import http.HttpRequest;

public class CgiHandler implements HttpHandler {

    private final ClientHandler client;
    private byte[] responseBytes;

    public CgiHandler(ClientHandler client) {
        this.client = client;
    }

    @Override
    public void handle() throws Exception {
        this.client.setIsResponseDone(true);
        this.client.getKey().interestOps(SelectionKey.OP_WRITE);

        HttpRequest request = client.getHttpRequest();

        String method = client.getHttpHeader().getMethod();
        String scriptPath = request.resolveFilePath();

        File script = new File(scriptPath);
        if (!script.exists()) {
            responseBytes = "<h1>404 Not Found</h1>".getBytes();
            return;
        }

        ProcessBuilder pb = new ProcessBuilder("python3", scriptPath);

        Map<String, String> env = pb.environment();
        env.put("REQUEST_METHOD", method);
        env.put("SCRIPT_NAME", scriptPath);
        env.put("SERVER_PROTOCOL", "HTTP/1.1");

        String contentLength = client.getHttpHeader().getHeaders().get("content-length");
        if (contentLength != null) {
            env.put("CONTENT_LENGTH", contentLength);
        }

        Process process = pb.start();
        if (method.equalsIgnoreCase("POST")) {
            try (
                    FileInputStream fis = new FileInputStream("body.tmp");
                    OutputStream os = process.getOutputStream();) {
                byte[] buffer = new byte[1024];
                int read;

                while ((read = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }

                os.flush();
            }
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            output.append(line).append("\r\n");
        }

        this.responseBytes = output.toString().getBytes();
    }

    @Override
    public void response() throws IOException {
        this.client.setIsResponseDone(true);
        this.client.getKey().interestOps(SelectionKey.OP_WRITE);

        if (this.responseBytes == null) {
            return;
        }

        Session session = client.getHttpRequest().getSession();

        String httpResponse = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: text/html\r\n" +
                "Set-Cookie: SESSION_ID=" + session.getId() + "; Path=/\r\n" +
                "Content-Length: " + responseBytes.length + "\r\n" +
                "\r\n";

        ByteBuffer buffer = ByteBuffer.allocate(httpResponse.length() + responseBytes.length);
        buffer.put(httpResponse.getBytes());
        buffer.put(responseBytes);
        buffer.flip();

        this.client.getClient().write(buffer);
    }

}

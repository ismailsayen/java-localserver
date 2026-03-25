package handlers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.file.Path;
import java.util.Map;
import Nio.ClientHandler;
import config.utils.Session;
import http.HttpHandler;
import http.HttpRequest;

public class CgiHandler implements HttpHandler {

    private final ClientHandler client;

    private Process process;
    private File outputFile;

    private boolean prepared = false;
    private FileChannel fileChannel;
    private long fileSize = 0;
    private long position = 0;
    private ByteBuffer headerBuffer;

    public CgiHandler(ClientHandler client) {
        this.client = client;
    }


    @Override
    public void handle() throws Exception {
        HttpRequest request = client.getHttpRequest();
        String method = client.getHttpHeader().getMethod();
        String scriptPath = request.resolveFilePath();

        File script = new File(scriptPath);
        if (!script.exists() || script.isDirectory() || !scriptPath.endsWith(".py")) {
            this.client.getHttpRequest().setStatus("404");
            throw new RuntimeException("Not found");
        }

        this.outputFile = File.createTempFile("cgi_out_", ".tmp");
        this.outputFile.deleteOnExit();

        ProcessBuilder pb = new ProcessBuilder("python3", scriptPath);
        pb.redirectErrorStream(true); 
        pb.redirectOutput(this.outputFile);

        Map<String, String> env = pb.environment();
        env.put("REQUEST_METHOD", method);
        env.put("SCRIPT_NAME", scriptPath);
        env.put("SERVER_PROTOCOL", "HTTP/1.1");

        String contentLength = client.getHttpHeader().getHeaders().get("content-length");
        if (contentLength != null) {
            env.put("CONTENT_LENGTH", contentLength);
        }

        this.process = pb.start();

        if (this.client.getBodyFileTempName() != null) {
            try (
                    FileInputStream fis = new FileInputStream("temp_uploads/" + this.client.getBodyFileTempName());
                    OutputStream os = this.process.getOutputStream()) {
                byte[] buffer = new byte[8096];
                int read;
                while ((read = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
        } else {
            this.process.getOutputStream().close();
        }

        this.client.getKey().interestOps(SelectionKey.OP_WRITE);
    }


    @Override
    public void response() throws IOException {

        if (process != null && this.process.isAlive()) {
            return;
        }

        if (!prepared) {
            this.fileSize = this.outputFile.length();
            this.fileChannel = FileChannel.open(Path.of(this.outputFile.getPath()));

            Session session = client.getHttpRequest().getSession();
            String headers = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: text/html\r\n" +
                    "Set-Cookie: SESSION_ID=" + session.getId() + "; Path=/\r\n" +
                    "Content-Length: " + this.fileSize + "\r\n" +
                    "Connection: close\r\n\r\n";

            this.headerBuffer = ByteBuffer.wrap(headers.getBytes());
            this.prepared = true;
            return;
        }

        if (this.headerBuffer != null && this.headerBuffer.hasRemaining()) {
            this.client.getClient().write(this.headerBuffer);
            if (this.headerBuffer.hasRemaining()) {
                return;
            }
            this.headerBuffer = null;
        }

        if (this.fileChannel != null) {
            long chunk = Math.min(64 * 1024, this.fileSize - this.position);
            long transferred = this.fileChannel.transferTo(this.position, chunk, this.client.getClient());

            if (transferred > 0) {
                this.position += transferred;
            }

            if (this.position >= this.fileSize) {
                finish();
            }
        }
    }

    private void finish() throws IOException {
        if (this.fileChannel != null) {
            this.fileChannel.close();
        }
        if (this.outputFile != null) {
            this.outputFile.delete();
        }

        this.client.setIsResponseDone(true);
        this.client.getKey().cancel();
    }
}
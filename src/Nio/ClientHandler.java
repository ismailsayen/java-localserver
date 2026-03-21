package Nio;

import DTO.Server;
import http.HttpHeader;
import http.HttpRequest;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class ClientHandler {
    private SocketChannel client;
    private Server virtualHosts;
    private SelectionKey key;
    private HttpRequest httpRequest;
    private HttpHeader headerHttp;
    private Boolean isHeadersFound = false;
    private Boolean isResponseDone = false;
    private ByteArrayOutputStream byteArrayOutputStream;
    private FileOutputStream fileOutputStream;
    private Long contentLength;
    private Long totalBodyBytes;
    private ByteBuffer buf;
    private String bodyTempFileName;
    private Boolean isStartReading;

    public ClientHandler(SocketChannel client, SelectionKey key, Server virtualHosts) {
        this.client = client;
        this.virtualHosts = virtualHosts;
        this.key = key;
        this.totalBodyBytes = 0L;
        this.isStartReading = false;
        buf = ByteBuffer.allocate(8096);
        this.byteArrayOutputStream = new ByteArrayOutputStream();
    }

    public void readHttpMessage() throws IOException {
        int bytesRead = this.client.read(buf);

        if (bytesRead == -1) {
            this.client.close();
            return;
        }

        if (bytesRead == 0)
            return;

        if (!this.isStartReading) {
            this.createdFileOutputStream();
            this.isStartReading = true;
        }

        buf.flip();
        if (!this.isHeadersFound) {
            byteArrayOutputStream.write(buf.array(), 0, bytesRead);
            this.readHeaders(byteArrayOutputStream);
        } else {
            totalBodyBytes += bytesRead;
            this.fileOutputStream.write(buf.array(), 0, bytesRead);
        }
        buf.clear();

        if (this.httpRequest != null && (this.contentLength == null || totalBodyBytes >= this.contentLength)) {
            this.fileOutputStream.close();
            try {
                this.httpRequest.executeHandler(this);
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }

    public void handleResponse() throws IOException {
        try {
            this.httpRequest.executeResponse(this);
        } catch (IOException e) {
            // TODO: handle exception
        }
        if (this.isResponseDone) {
            Path bodyPath = Paths.get("temp_uploads", this.bodyTempFileName);
            Files.deleteIfExists(bodyPath);
            this.client.close();
        }
    }

    public void readHeaders(ByteArrayOutputStream byteArrayOutputStream) {
        byte[] data = byteArrayOutputStream.toByteArray();
        String message = new String(data, StandardCharsets.UTF_8);
        int index = message.indexOf("\r\n\r\n");

        if (index != -1) {
            this.isHeadersFound = true;
            this.headerHttp = HttpHeader.parseHeaders(message.substring(0, index));
            this.httpRequest = new HttpRequest(headerHttp, this.virtualHosts);
            try {
                this.httpRequest.HandleRequest(this);
                byteArrayOutputStream.reset();
                this.fileOutputStream.write(data, index + 4, data.length - (index + 4));
                this.totalBodyBytes += data.length - (index + 4);
            } catch (Exception e) {
                return;
            }

            String cl = this.headerHttp.getHeaders().get("content-length");

            if (cl != null) {
                this.contentLength = Long.parseLong(this.headerHttp.getHeaders().get("content-length"));
            }

        }
    }

    private void createdFileOutputStream() {
        try {
            String uniqueId = UUID.randomUUID().toString();
            this.bodyTempFileName = "body" + uniqueId + ".tmp";
            Path tempPath = Paths.get("temp_uploads", this.bodyTempFileName);
            Files.createDirectories(tempPath.getParent());
            this.fileOutputStream = new FileOutputStream(tempPath.toFile());
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    @Override
    public String toString() {
        return "Hello client";
    }

    public SocketChannel getClient() {
        return client;
    }

    public void setClient(SocketChannel client) {
        this.client = client;
    }

    public Server getVirtualHosts() {
        return virtualHosts;
    }

    public void setVirtualHosts(Server virtualHosts) {
        this.virtualHosts = virtualHosts;
    }

    public HttpRequest getHttpRequest() {
        return httpRequest;
    }

    public void setHttpRequest(HttpRequest httpRequest) {
        this.httpRequest = httpRequest;
    }

    public HttpHeader getHttpHeader() {
        return headerHttp;
    }

    public void setHttpHeader(HttpHeader headerHttp) {
        this.headerHttp = headerHttp;
    }

    public SelectionKey getKey() {
        return key;
    }

    public void setKey(SelectionKey key) {
        this.key = key;
    }

    public Boolean getIsResponseDone() {
        return this.isResponseDone;
    }

    public void setIsResponseDone(Boolean value) {
        this.isResponseDone = value;
    }

    public ByteArrayOutputStream getByteArrayOutputStream() {
        return this.byteArrayOutputStream;
    }

    public void setByteArrayOutputStream(ByteArrayOutputStream byteArrayOutputStream) {
        this.byteArrayOutputStream = byteArrayOutputStream;
    }

    public FileOutputStream getFileOutputStream() {
        return this.fileOutputStream;
    }

    public void setFileOutputStream(FileOutputStream fileOutputStream) {
        this.fileOutputStream = fileOutputStream;
    }

    public String getBodyFileTempName() {
        return this.bodyTempFileName;
    }

    public void setBodyFileTempName(String value) {
        this.bodyTempFileName = value;
    }
}
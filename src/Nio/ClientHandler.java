package Nio;

import DTO.Server;
import handlers.ErrorHandler;
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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ClientHandler {
    private SocketChannel client;
    private List<Server> virtualHosts;
    private Server server;
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
    private Boolean isChunked;
    private Boolean isBodyExists;
    private ByteArrayOutputStream chunkBuffer = new ByteArrayOutputStream();
    private int remainingChunkSize = -1;
    private boolean isRequestDone = false;
    private ErrorHandler error;

    public ClientHandler(SocketChannel client, SelectionKey key, List<Server> virtualHosts) {
        this.client = client;
        this.virtualHosts = virtualHosts;
        this.key = key;
        this.totalBodyBytes = 0L;
        this.isStartReading = false;
        this.isChunked = false;
        this.isBodyExists = false;
        buf = ByteBuffer.allocate(8096);
        this.byteArrayOutputStream = new ByteArrayOutputStream();
        this.error = new ErrorHandler(this);
    }

    public void readHttpMessage() throws IOException {
        int bytesRead = this.client.read(buf);

        if (bytesRead == -1) {
            this.client.close();
            return;
        }

        if (bytesRead == 0)
            return;

        buf.flip();
        if (!this.isHeadersFound) {
            byteArrayOutputStream.write(buf.array(), 0, bytesRead);
            this.readHeaders(byteArrayOutputStream);
        } else if (this.isBodyExists) {
            this.readBody(Arrays.copyOf(this.buf.array(), bytesRead));
        }
        buf.clear();

        if (this.totalBodyBytes > this.server.getLimitRequestBody()) {
            this.error.error("400", "Data too much large");
            this.deleteTempFile();
            this.client.close();
            return;
        }


        if (this.contentLength != null && totalBodyBytes >= this.contentLength) {
            this.isRequestDone = true;
        }

        if (this.httpRequest != null && this.isRequestDone) {
            try {
                if (this.fileOutputStream != null) {
                    this.fileOutputStream.close();
                }
                this.httpRequest.executeHandler(this);
            } catch (Exception e) {
                this.error.error(httpRequest.getStatus(), e.getMessage());
                this.deleteTempFile();
                this.client.close();
            }
        }
    }

    public void handleResponse() throws IOException {
        try {
            this.httpRequest.executeResponse(this);
        } catch (Exception e) {
            this.error.error(httpRequest.getStatus(), e.getMessage());
            this.client.close();
        }
        if (this.isResponseDone) {
            this.deleteTempFile();
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
            this.server = selectServer();
            this.httpRequest = new HttpRequest(headerHttp, this.server);
            String cl = this.headerHttp.getHeaders().get("content-length");
            String te = this.headerHttp.getHeaders().get("transfer-encoding");
            if (te != null && te.equals("chunked")) {
                this.isChunked = true;
                this.isBodyExists = true;
            } else if (cl != null) {
                this.contentLength = Long.valueOf(this.headerHttp.getHeaders().get("content-length"));
                this.isBodyExists = true;
            } else {
                this.isRequestDone = true;
            }

            try {
                this.httpRequest.HandleRequest(this);
                byteArrayOutputStream.reset();
                if (this.isBodyExists) {
                    int bodyStart = index + 4;
                    if (bodyStart < data.length) {
                        byte[] bodyPart = Arrays.copyOfRange(data, bodyStart, data.length);
                        this.readBody(bodyPart);
                    }
                }
            } catch (Exception e) {
                this.error.error(httpRequest.getStatus(), e.getMessage());
            }
        }
    }

    private String getHostFromHeader() {
        String host = this.headerHttp.getHeaders().get("host");

        if (host == null)
            return null;

        // remove port if exists (example: test.com:80)
        if (host.contains(":")) {
            host = host.split(":")[0];
        }

        return host.trim();
    }

    private Server selectServer() {
        String host = getHostFromHeader();

        if (host == null) {
            return getDefaultServer(); // default server
        }

        for (Server s : virtualHosts) {
            if (host.equalsIgnoreCase(s.getName())) {
                return s;
            }
        }

        return getDefaultServer(); // fallback
    }

    private Server getDefaultServer() {

        for (Server s : virtualHosts) {
            if (s.getDefaultServer()) {
                return s;
            }
        }

        return virtualHosts.get(0);
    }

    private void readBody(byte[] data) throws IOException {
        if (!this.isStartReading) {
            this.createdFileOutputStream();
            this.isStartReading = true;
        }

        if (this.isChunked) {
            chunkBuffer.write(data);
            byte[] currentBuffer = chunkBuffer.toByteArray();
            int offset = 0;

            while (offset < currentBuffer.length) {
                if (remainingChunkSize == -1) {
                    int crlfIdx = indexOf(currentBuffer, "\r\n".getBytes(), offset);
                    if (crlfIdx != -1) {
                        String sizeStr = new String(Arrays.copyOfRange(currentBuffer, offset, crlfIdx)).trim();
                        if (!sizeStr.isEmpty()) {
                            remainingChunkSize = Integer.parseInt(sizeStr, 16);
                            offset = crlfIdx + 2;

                            if (remainingChunkSize == 0) {
                                this.isRequestDone = true;
                                break;
                            }
                        } else {
                            offset = crlfIdx + 2;
                        }
                    } else {
                        break;
                    }
                }

                if (remainingChunkSize > 0) {
                    int availableData = currentBuffer.length - offset;
                    int bytesToRead = Math.min(remainingChunkSize, availableData);

                    if (bytesToRead > 0) {
                        this.fileOutputStream.write(currentBuffer, offset, bytesToRead);
                        this.totalBodyBytes += bytesToRead;
                        remainingChunkSize -= bytesToRead;
                        offset += bytesToRead;
                    }

                    if (remainingChunkSize == 0) {
                        if (offset + 2 <= currentBuffer.length) {
                            offset += 2;
                            remainingChunkSize = -1;
                        } else {
                            break;
                        }
                    }
                }
            }
            chunkBuffer.reset();
            if (offset < currentBuffer.length) {
                chunkBuffer.write(currentBuffer, offset, currentBuffer.length - offset);
            }
        } else {
            this.fileOutputStream.write(data, 0, data.length);
            this.totalBodyBytes += data.length;
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

    private int indexOf(byte[] data, byte[] pattern, int start) {
        for (int i = start; i <= data.length - pattern.length; i++) {
            boolean found = true;
            for (int j = 0; j < pattern.length; j++) {
                if (data[i + j] != pattern[j]) {
                    found = false;
                    break;
                }
            }
            if (found)
                return i;
        }
        return -1;
    }

    private void deleteTempFile() throws IOException {
        if (this.bodyTempFileName != null) {
            Path bodyPath = Paths.get("temp_uploads", this.bodyTempFileName);
            Files.deleteIfExists(bodyPath);
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

    public List<Server> getVirtualHosts() {
        return virtualHosts;
    }

    public void setVirtualHosts(List<Server> virtualHosts) {
        this.virtualHosts = virtualHosts;
    }

    public Server getServer() {
        return this.server;
    }

    public void setServer(Server server) {
        this.server = server;
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

    public ErrorHandler getErrorHandler() {
        return this.error;
    }

    public void setErrorPages(ErrorHandler error) {
        this.error = error;
    }

    public ByteBuffer getBuf() {
        return buf;
    }

    public void setBuf(ByteBuffer buf) {
        this.buf = buf;
    }

    public ByteArrayOutputStream getChunkBuffer() {
        return chunkBuffer;
    }

    public void setChunkBuffer(ByteArrayOutputStream chunkBuffer) {
        this.chunkBuffer = chunkBuffer;
    }
}
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
    private Boolean isChunked;
    private Boolean isBodyExists;
    private ByteArrayOutputStream chunkBuffer = new ByteArrayOutputStream();
    private int remainingChunkSize = -1;
    private boolean isRequestDone = false;
    private ErrorHandler error;

    public ClientHandler(SocketChannel client, SelectionKey key, Server virtualHosts) {
        this.client = client;
        this.virtualHosts = virtualHosts;
        this.key = key;
        this.totalBodyBytes = 0L;
        this.isStartReading = false;
        this.isChunked = false;
        this.isBodyExists = false;
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

        if(this.totalBodyBytes>this.virtualHosts.getLimitRequestBody()){
            //Error_body_large
            return;
        }

        buf.flip();
        if (!this.isHeadersFound) {
            byteArrayOutputStream.write(buf.array(), 0, bytesRead);
            this.readHeaders(byteArrayOutputStream);
        } else if (this.isBodyExists) {
            this.readBody(Arrays.copyOf(this.buf.array(), bytesRead));
        }
        buf.clear();

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
                System.out.println(e);
            }
        }
    }

    public void handleResponse() throws IOException {
        this.error=new ErrorHandler(this);
        try {
            this.httpRequest.executeResponse(this);
        } catch (Exception e) {
            System.out.println(e);
        }
        if (this.isResponseDone) {
            if (this.bodyTempFileName != null) {
                Path bodyPath = Paths.get("temp_uploads", this.bodyTempFileName);
                Files.deleteIfExists(bodyPath);
            }
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
            String cl = this.headerHttp.getHeaders().get("content-length");
            String te = this.headerHttp.getHeaders().get("transfer-encoding");
            if (te != null && te.equals("chunked")) {
                this.isChunked = true;
                this.isBodyExists = true;
            } else if (cl != null) {
                this.contentLength = Long.parseLong(this.headerHttp.getHeaders().get("content-length"));
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
                throw new RuntimeException(e);
            }
        }
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

    public ErrorHandler getErrorHandler() {
        return this.error;
    }

    public void setErrorPages(ErrorHandler error) {
        this.error = error;
    }
}
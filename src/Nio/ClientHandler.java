package Nio;

import DTO.Server;
import http.HttpHeader;
import http.HttpRequest;
import http.RequestStatus;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class ClientHandler {
    private SocketChannel client;
    private Server virtualHosts;
    private Long lastActivity;
    private ByteArrayOutputStream bodyAccumulator;
    private Boolean headersFounded;
    private ByteBuffer bufferReader;
    private HttpRequest httpRequest;
    private int totalByteRead = 0;
    private int contentLength = 0;

    public ClientHandler(SocketChannel client, Server virtualHosts) {
        this.client = client;
        this.virtualHosts = virtualHosts;
        this.headersFounded = false;
        this.bufferReader = ByteBuffer.allocate(4096);
        this.bodyAccumulator = new ByteArrayOutputStream();
    }

    public void read() throws IOException {
        this.lastActivity = System.currentTimeMillis();
        int bytesRead = this.client.read(bufferReader);
        if (bytesRead == -1) {
            this.client.close();
            return;
        }
        this.totalByteRead += bytesRead;
        this.bufferReader.flip();
        byte[] data = new byte[bufferReader.remaining()];
        bufferReader.get(data);
        this.bodyAccumulator.write(data);
        bufferReader.clear();
        if (!headersFounded) {
            byte[] fullData = bodyAccumulator.toByteArray();
            String req = new String(fullData);
            int index = req.indexOf("\r\n\r\n");
            if (index != -1) {
                HttpHeader headerHttp = new HttpHeader().parseHeaders(req.substring(0,
                        index));
                this.httpRequest = new HttpRequest(headerHttp);
                this.contentLength = httpRequest.getContentLength();
                headersFounded = true;
                int bodyStart = index + 4;
                int bodyTotal = fullData.length - bodyStart;
                this.totalByteRead = bodyTotal;
                bodyAccumulator.reset();
                if (bodyTotal > 0) {
                    bodyAccumulator.write(data, bodyStart, bodyTotal);

                }
                // System.out.println("headers==>\n" + headerHttp.getHeaders());

            }
        } else {
            if (this.httpRequest.getStatus() == RequestStatus.READY) {
                System.out.println("req ready for been write");
                return;
            }

            if (this.httpRequest.getStatus() == RequestStatus.PROCESSING) {
                if (this.contentLength <= this.totalByteRead) {
                    byte[] fullData = bodyAccumulator.toByteArray();
                    String req = new String(fullData);
                    System.out.println("Body====>\n" + req);
                    // System.out.println(httpRequest.getBoundary());
                }
            }
        }
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

    public Long getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(Long lastActivity) {
        this.lastActivity = lastActivity;
    }

    public ByteBuffer getBuffer() {
        return bufferReader;
    }

    public void setBuffer(ByteBuffer bufferReader) {
        this.bufferReader = bufferReader;
    }

    public ByteArrayOutputStream getByteArrayOutputStream() {
        return bodyAccumulator;
    }

    public void setByteArrayOutputStream(ByteArrayOutputStream bodyAccumulator) {
        this.bodyAccumulator = bodyAccumulator;
    }

    public HttpRequest getHttpRequest() {
        return httpRequest;
    }

    public void setHttpRequest(HttpRequest httpRequest) {
        this.httpRequest = httpRequest;
    }

}

package Nio;

import DTO.Server;
import http.HttpHeader;
import http.HttpRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class ClientHandler {
    private SocketChannel client;
    private Server virtualHosts;
    private Long lastActivity;
    private ByteArrayOutputStream byteArrayOutputStream;
    private Boolean headersFounded;
    private ByteBuffer bufferReader;
    private HttpRequest httpRequest;
    private int contentLength = 0;

    public ClientHandler(SocketChannel client, Server virtualHosts) {
        this.client = client;
        this.virtualHosts = virtualHosts;
        this.headersFounded = false;
        this.bufferReader = ByteBuffer.allocate(2048);
        this.byteArrayOutputStream = new ByteArrayOutputStream();
    }

    public void read() throws IOException {
        this.lastActivity = System.currentTimeMillis();
        int bytesRead = this.client.read(bufferReader);
        if (bytesRead == -1) {
            this.client.close();
            return;
        }

        bufferReader.flip();
        byte[] data = new byte[bufferReader.remaining()];
        bufferReader.get(data);
        this.byteArrayOutputStream.write(data);
        bufferReader.clear();
        if (!headersFounded) {
            byte[] fullData = byteArrayOutputStream.toByteArray();
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
                byteArrayOutputStream.reset();
                byteArrayOutputStream.write(data, bodyStart, bodyTotal);
            }
        } else {
            // Gestion du body simple pour l'instant
            System.out.println("Lecture du body... Taille totale : " + new String(byteArrayOutputStream.toByteArray()));
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
        return byteArrayOutputStream;
    }

    public void setByteArrayOutputStream(ByteArrayOutputStream byteArrayOutputStream) {
        this.byteArrayOutputStream = byteArrayOutputStream;
    }

    public HttpRequest getHttpRequest() {
        return httpRequest;
    }

    public void setHttpRequest(HttpRequest httpRequest) {
        this.httpRequest = httpRequest;
    }
}

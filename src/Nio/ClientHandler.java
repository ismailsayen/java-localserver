package Nio;

import DTO.Server;
import http.HttpHeader;
import http.HttpRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class ClientHandler {
    private SocketChannel client;
    private Server virtualHosts;
    private HttpRequest httpRequest;
    private HttpHeader headerHttp;
    private Boolean isHeadersFound = false;

    public ClientHandler(SocketChannel client, Server virtualHosts) {
        this.client = client;
        this.virtualHosts = virtualHosts;
    }

    public void readHttpMessage() throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(60);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        int bytesRead = this.client.read(buf);
        if (bytesRead <= 0) {
            this.client.close();
            return;
        }

        while (bytesRead > 0) {
            buf.flip();

            byteArrayOutputStream.write(buf.array(), 0, bytesRead);
            if (!this.isHeadersFound) {
                this.readHeaders(byteArrayOutputStream);
            } else {
                this.readBody(byteArrayOutputStream);
            }

            buf.clear();
            bytesRead = this.client.read(buf);
        }

        try {
            this.httpRequest.setBody(byteArrayOutputStream.toByteArray());
            this.httpRequest.executeHandler(this);
        } catch (Exception e) {
            System.out.println(e);
        }
        client.close();
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
                this.httpRequest.HandleRequest();
            } catch (Exception e) {
                return;
            }

            byteArrayOutputStream.reset();
            byteArrayOutputStream.write(data, index + 4, data.length - (index + 4));
        }
    }

    public void readBody(ByteArrayOutputStream byteArrayOutputStream) {

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

}

package Nio;

import DTO.Server;
import http.HttpHeader;
import http.HttpRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class ClientHandler {
    private SocketChannel client;
    private Server virtualHosts;
    private SelectionKey key;
    private HttpRequest httpRequest;
    private HttpHeader headerHttp;
    private Boolean isHeadersFound = false;
    private Boolean isBodyFound = false;

    public ClientHandler(SocketChannel client, SelectionKey key, Server virtualHosts) {
        this.client = client;
        this.virtualHosts = virtualHosts;
        this.key = key;
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

            if (this.isBodyFound) {
                break;
            }

            bytesRead = this.client.read(buf);
        }

        if (!this.isBodyFound) {
            this.readBody(byteArrayOutputStream);
        }

        try {
            this.httpRequest.setBody(byteArrayOutputStream.toByteArray());
            this.httpRequest.executeHandler(this);
        } catch (Exception e) {
            System.out.println(e);
        }
        // client.close();
    }

    public void  handleResponse()  throws IOException{
        try {
            this.httpRequest.executeResponse(this);
        } catch (Exception e) {
            // TODO: handle exception
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
            } catch (Exception e) {
                return;
            }

            byteArrayOutputStream.reset();
            byteArrayOutputStream.write(data, index + 4, data.length - (index + 4));
        }
    }

    public void readBody(ByteArrayOutputStream byteArrayOutputStream) {
        String contentLength = this.headerHttp.getHeaders().get("content-length");

        if (contentLength != null) {
            int cl = Integer.parseInt(contentLength);
            if (byteArrayOutputStream.size() >= cl) {
                byte[] data = byteArrayOutputStream.toByteArray();
                this.isBodyFound = true;
                byteArrayOutputStream.reset();
                byteArrayOutputStream.write(data, 0, cl);
            }
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

    public SelectionKey getKey() {
        return key;
    }

    public void setKey(SelectionKey key) {
        this.key = key;
    }

}

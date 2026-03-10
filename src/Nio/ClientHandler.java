package Nio;

import DTO.Server;
import http.HttpHeader;
import http.HttpRequest;
import http.RequestStatus;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class ClientHandler {
    private SocketChannel client;
    private Server virtualHosts;
    private Long lastActivity;
    private ByteArrayOutputStream bodyAccumulator;
    private Boolean headersFounded;
    private ByteBuffer bufferReader;
    private HttpRequest httpRequest;
    private int totalByteRead = 0;
    private Long contentLength = 0L;

    public ClientHandler(SocketChannel client, Server virtualHosts) {
        this.client = client;
        this.virtualHosts = virtualHosts;
        this.headersFounded = false;
        this.bufferReader = ByteBuffer.allocate(1024);
        this.bodyAccumulator = new ByteArrayOutputStream();
    }

    public void read() throws IOException {
        this.lastActivity = System.currentTimeMillis();
        int bytesRead = this.client.read(bufferReader);

        if (bytesRead == -1) {
            this.client.close();
            return;
        }
        if (bytesRead == 0)
            return;

        this.totalByteRead += bytesRead;
        this.bufferReader.flip();
        byte[] data = new byte[bufferReader.remaining()];
        bufferReader.get(data);
        this.bodyAccumulator.write(data);
        bufferReader.clear();

        if (!headersFounded) {
            parseHeaders();
        }

        if (headersFounded) {
            if (this.httpRequest.getStatus() == RequestStatus.READY) {
                // System.out.println("ssss");
                sendHelloResponse();
            } else if (this.httpRequest.getStatus() == RequestStatus.PROCESSING) {
                // Vérifier si on a fini de lire le body
                if (this.bodyAccumulator.size() >= this.contentLength) {
                    String body = new String(bodyAccumulator.toByteArray());
                    System.out.println(body);
                }
            }
        }
    }

    private void parseHeaders() {
        byte[] fullData = bodyAccumulator.toByteArray();
        String req = new String(fullData, StandardCharsets.UTF_8);
        int index = req.indexOf("\r\n\r\n");

        if (index != -1) {
            HttpHeader headerHttp = HttpHeader.parseHeaders(req.substring(0, index));
            this.httpRequest = new HttpRequest(headerHttp, this.virtualHosts);
            try {
                this.httpRequest.HandleRequest();
            } catch (Exception e) {
                System.out.println("=>>>>>>>" + e.getMessage());
                return;
            }
            this.contentLength = (long) httpRequest.getContentLength();
            this.headersFounded = true;

            int bodyStart = index + 4;
            int bodyTotal = fullData.length - bodyStart;

            bodyAccumulator.reset();
            if (bodyTotal > 0) {
                bodyAccumulator.write(fullData, bodyStart, bodyTotal);
            }
            this.totalByteRead = bodyTotal;
        }

    }

    private void sendHelloResponse() throws IOException {
        String html = "<html><body><h1>wa Akhiiiiiiiiiiiiran </h1></body></html>";
        byte[] bodyBytes = html.getBytes(StandardCharsets.UTF_8);

        // Utilisation d'une String simple pour éviter les problèmes d'indentation des
        // Text Blocks
        String responseHeader = """
                HTTP/1.1 200 OK\r
                Content-Type: text/html\r
                Content-Length: """ + bodyBytes.length + "\r\n" +
                "Connection: close\r\n" +
                "\r\n";

        byte[] headerBytes = responseHeader.getBytes(StandardCharsets.UTF_8);
        ByteBuffer respBuffer = ByteBuffer.allocate(headerBytes.length + bodyBytes.length);
        respBuffer.put(headerBytes);
        respBuffer.put(bodyBytes);
        respBuffer.flip();

        while (respBuffer.hasRemaining()) {
            client.write(respBuffer);
        }

        // System.out.println("Réponse envoyée, fermeture du client.");
        client.close();
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

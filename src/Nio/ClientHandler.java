package Nio;

import DTO.Server;
import http.httpParser;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;

public class ClientHandler {
    private SocketChannel client;
    private List<Server> virtualHosts;
    private Long lastActivity;
    private httpParser http;

    public ClientHandler(SocketChannel client, List<Server> virtualHosts) {
        this.client = client;
        this.virtualHosts = virtualHosts;
    }

    public void read() throws IOException {
        this.lastActivity = System.currentTimeMillis();
        if (this.http == null) {
            System.out.println("khawya");
        } else {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            int bytesRead = this.client.read(buffer);
            if (bytesRead == -1) {
                this.client.close();
                return;
            }
            buffer.flip();
            String request = new String(buffer.array(), buffer.position(), bytesRead);
            String[] req = request.split("\r\n\r\n");
            // // System.out.println(request);
            // //
            //
            System.out.println(
                    "++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
            // String[] sub = Arrays.copyOfRange(req, 0, 1);

            for (String line : req) {
                // String[] l =line.split(":");
                System.out.println("====>" + line);
            }
        }
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

    public Long getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(Long lastActivity) {
        this.lastActivity = lastActivity;
    }

    public httpParser getHttp() {
        return http;
    }

    public void setHttp(httpParser http) {
        this.http = http;
    }
}

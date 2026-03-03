package Nio;

import java.nio.channels.SocketChannel;
import java.util.List;

import DTO.Server;

public class ClientHandler {
    private SocketChannel client;
    private List<Server> virtualHosts;
    private Long lastActivity;

    public ClientHandler(SocketChannel client, List<Server> virtualHosts) {
        this.client = client;
        this.virtualHosts = virtualHosts;
        this.lastActivity = System.currentTimeMillis();
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
}

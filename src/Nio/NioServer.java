package Nio;

import DTO.Server;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public class NioServer {
    private Selector selector;
    public Integer count = 1;
    private LinkedHashSet<Object> ppp;

    public void start(List<Server> serverConfig) throws IOException {
        selector = Selector.open();

        createTcpListeners(serverConfig);

        while (true) {
            selector.select();

            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();

            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();

                try {
                    if (!key.isValid())
                        continue;

                    if (key.isAcceptable()) {
                        ServerSocketChannel channel = (ServerSocketChannel) key.channel();
                        List<Server> virtualHost = (List<Server>) key.attachment();
                        handleAccept(channel, virtualHost);
                    } else if (key.isReadable()) {
                        handleRead(key);
                    } else if (key.isWritable()) {
                        handleWrite(key);
                    }

                } catch (Exception e) {
                    handleKeyError(key, e);
                }
            }
        }
    }

    private void handleAccept(ServerSocketChannel channel, List<Server> virtualHost)
            throws IOException {
        SocketChannel client = channel.accept();
        client.configureBlocking(false);
        SelectionKey key = client.register(selector, SelectionKey.OP_READ);
        ClientHandler handlerClient = new ClientHandler(client, key, virtualHost);
        key.attach(handlerClient);
    }

    private void handleRead(SelectionKey key) throws IOException {
        ClientHandler client = (ClientHandler) key.attachment();
        client.readHttpMessage();
    }

    private void handleWrite(SelectionKey key) throws IOException {
        ClientHandler client = (ClientHandler) key.attachment();
        client.handleResponse();
    }

    private void createTcpListeners(List<Server> serverConfig) throws IOException {
        Map<String, List<Server>> grouped = groupServers(serverConfig);
        for (Map.Entry<String, List<Server>> entry : grouped.entrySet()) {
            String[] parts = entry.getKey().split(":");
            String host = parts[0];
            int port = Integer.parseInt(parts[1]);
            if (!host.equals("0.0.0.0") && this.ppp.contains(port)) {
                continue;
            }
            ServerSocketChannel scc = ServerSocketChannel.open();
            scc.configureBlocking(false);
            scc.bind(new InetSocketAddress(host, port));
            scc.register(selector, SelectionKey.OP_ACCEPT, entry.getValue());
            System.out.println("   👉 http://" + host + ":" + port);
            System.out.println();
        }
    }

    private Map<String, List<Server>> groupServers(List<Server> configs) {
        Map<String, List<Server>> map = new HashMap<>();

        for (Server s : configs) {
            if (s.getHost().equals("0.0.0.0")) {
                this.ppp = s.getPort();
            }
            for (Object port : s.getPort()) {
                String key = s.getHost() + ":" + port;
                map.computeIfAbsent(key, k -> new ArrayList<>()).add(s);
            }
        }

        return map;
    }

    private void handleKeyError(SelectionKey key, Exception e) {
        try {
            System.err.println("Connection error: " + e.getMessage());

            // Close channel safely
            if (key.channel() != null) {
                key.channel().close();
            }

        } catch (IOException closeEx) {
            System.err.println("Error closing channel: " + closeEx.getMessage());
        } finally {
            // Always cancel the key
            key.cancel();
        }
    }
}

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
import java.util.List;
import java.util.Map;

public class NioServer {
    private Selector selector;
    public Integer count = 1;

    public void start(List<Server> serverConfig) throws IOException {
        selector = Selector.open();

        createTcpListeners(serverConfig);

        while (true) {

            selector.select();

            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();

            while (iterator.hasNext()) {

                SelectionKey key = iterator.next();
                iterator.remove();

                if (!key.isValid())
                    continue;

                if (key.isAcceptable()) {
                    ServerSocketChannel channel = (ServerSocketChannel) key.channel();
                    List<Server> virtualHost = (List<Server>) key.attachment();
                    handleAccept(channel, virtualHost);
                    continue;
                }

                if (key.isReadable()) {
                    handleRead(key);
                    continue;
                }

                if (key.isWritable()) {
                    handleWrite(key);
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

            ServerSocketChannel scc = ServerSocketChannel.open();
            scc.configureBlocking(false);
            scc.bind(new InetSocketAddress(port));

            scc.register(selector, SelectionKey.OP_ACCEPT, entry.getValue());
        }
    }

    private Map<String, List<Server>> groupServers(List<Server> configs) {
        Map<String, List<Server>> map = new HashMap<>();

        for (Server s : configs) {
            for (Object port : s.getPort()) {
                String key = s.getHost() + ":" + port;
                map.computeIfAbsent(key, k -> new ArrayList<>()).add(s);
            }
        }

        return map;
    }
}

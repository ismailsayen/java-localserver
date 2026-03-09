package Nio;

import DTO.Server;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NioServer {
    private Selector selector;
    public Integer count = 1;

    public void start(List<Server> serverConfig) throws IOException {
        selector = Selector.open();
        createTcpListeners(serverConfig);

        while (true) {
            if (selector.select() == 0)
                continue;

            for (var key : selector.selectedKeys()) {
                if (key.isAcceptable()) {
                    if (key.channel() instanceof ServerSocketChannel channel) {
                        Server virtualHost = (Server) key.attachment();
                        handleAccept(channel, virtualHost);
                    }
                }
                if (key.isReadable()) {
                    if (key.channel() instanceof SocketChannel) {
                        handleRead(key);
                    }
                }
            }
            selector.selectedKeys().clear();
        }
    }

    private void handleAccept(ServerSocketChannel channel, Server virtualHost) throws IOException {
        SocketChannel client = channel.accept();
        client.configureBlocking(false);
        SelectionKey key = client.register(selector, SelectionKey.OP_READ);
        ClientHandler handlerClient = new ClientHandler(client, virtualHost);
        key.attach(handlerClient);
    }

    private void handleRead(SelectionKey key) throws IOException {
        ClientHandler client = (ClientHandler) key.attachment();
        client.read();
    }

    private void createTcpListeners(List<Server> serverConfig) throws IOException {
        Map<String, Server> BindData = new HashMap<>();
        for (Server config : serverConfig) {
            for (Object port : config.getPort()) {
                String dns = config.getHost() + ":" + port;
                BindData.put(dns, config);
            }
        }

        for (Map.Entry<String, Server> serv : BindData.entrySet()) {
            String dns = serv.getKey();
            String[] parts = dns.split(":");
            // String host = parts[0];
            int port = Integer.parseInt(parts[1]);
            ServerSocketChannel scc = ServerSocketChannel.open();
            scc.configureBlocking(false);
            scc.bind(new InetSocketAddress(port));
            scc.register(selector, SelectionKey.OP_ACCEPT, serv.getValue());
        }
    }
}

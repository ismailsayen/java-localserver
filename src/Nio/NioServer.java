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
    public Integer count = 1;

    public void start(List<Server> serverConfig) throws IOException {

        var serverSocketChannel = ServerSocketChannel.open();
        var selector = Selector.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress(8080));
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (true) {
            System.out.println("aaaa");
            if (selector.select() == 0)
                continue;

            for (var key : selector.selectedKeys()) {

            }
            selector.selectedKeys().clear();
        }
    }

    // private void handleAccept(ServerSocketChannel channel, Server virtualHost)
    // throws IOException {
    // SocketChannel client = channel.accept();
    // client.configureBlocking(false);
    // SelectionKey key = client.register(selector, SelectionKey.OP_READ);
    // ClientHandler handlerClient = new ClientHandler(client, virtualHost);
    // key.attach(handlerClient);
    // }

    // private void handleRead(SelectionKey key) throws IOException {
    // ClientHandler cl = (ClientHandler) key.attachment();
    // cl.read();
    // }
}

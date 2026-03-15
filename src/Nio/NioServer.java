package Nio;

import DTO.Server;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.List;

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
                    Server virtualHost = (Server) key.attachment();
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

    private void handleAccept(ServerSocketChannel channel, Server virtualHost)
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
        for (Server config : serverConfig) {
            for (Object port : config.getPort()) {
                ServerSocketChannel scc = ServerSocketChannel.open();
                scc.configureBlocking(false);
                scc.bind(new InetSocketAddress(Integer.parseInt("" + port)));
                scc.register(selector, SelectionKey.OP_ACCEPT, config);
            }
        }
    }
}

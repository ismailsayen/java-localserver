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
import java.util.List;
import java.util.Map;

public class NioServer {
    private Selector selector;
    public Integer count = 1;

    public void start(List<Server> serverConfig) throws IOException {

        selector = Selector.open();

        Map<String, List<Server>> BindData = new HashMap<>();
        for (Server config : serverConfig) {
            String host = config.getHost();
            for (Object port : config.getPort()) {
                String dns = host + ":" + port;
                BindData.computeIfAbsent(dns, k -> new ArrayList<>()).add(config);
            }
        }

        for (Map.Entry<String, List<Server>> serv : BindData.entrySet()) {
            String dns = serv.getKey();
            String[] parts = dns.split(":");
            String host = parts[0];
            int port = Integer.parseInt(parts[1]);
            ServerSocketChannel scc = ServerSocketChannel.open();
            scc.configureBlocking(false);
            scc.bind(new InetSocketAddress(host, port));
            scc.register(selector, SelectionKey.OP_ACCEPT, serv.getValue());
            System.out.println("http://" + dns);
        }

        while (true) {

            selector.select();
            for (var key : selector.selectedKeys()) {
                if (key.isAcceptable()) {
                    if (key.channel() instanceof ServerSocketChannel channel) {
                        List<Server> virtualHost = (List<Server>) key.attachment();
                        handleAccept(channel, virtualHost);
                    }
                } else if (key.isReadable()) {
                    if (key.channel() instanceof SocketChannel) {
                        // ByteBuffer buffer = ByteBuffer.allocate(1024);
                        // int bytesRead = client.read(buffer);
                        // if (bytesRead == -1) {
                        //     client.close();
                        //     break;
                        // }
                        // buffer.flip();
                        // String request = new String(buffer.array(), buffer.position(), bytesRead);
                        // String[] req = request.split("\r\n\r\n");
                        // // // System.out.println(request);
                        // // //
                        // // System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
                        // // String[] sub = Arrays.copyOfRange(req, 0, 1);

                        // for (String line : req) {
                        //     // String[] l =line.split(":");
                        //     System.out.println(count + "====>" + line);
                        //     count++;
                        // }
                        handleRead(key);
                    }
                } else {
                    System.out.println("ikhan");
                }
            }
            selector.selectedKeys().clear();
        }
    }

    private void handleAccept(ServerSocketChannel channel, List<Server> virtualHost) throws IOException {
        SocketChannel client = channel.accept();
        client.configureBlocking(false);
        SelectionKey key = client.register(selector, SelectionKey.OP_READ);
        ClientHandler handlerClient = new ClientHandler(client, virtualHost);
        key.attach(handlerClient);
    }

    private void handleRead(SelectionKey key) throws IOException {
        ClientHandler cl = (ClientHandler) key.attachment();
        cl.read();
    }
}

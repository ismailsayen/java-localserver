package NioServer;

import DTO.Server;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NioServer {
    public void start(List<Server> serverConfig) throws IOException {
        Selector selector = Selector.open();

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
            System.out.println("ss");
            for (var key : selector.selectedKeys()) {
                if (key.isAcceptable()) {
                    if (key.channel() instanceof ServerSocketChannel channel) {
                        var client = channel.accept();
                        var socket = client.socket();
                        System.out.println("acceptable" + socket.getRemoteSocketAddress());
                        client.configureBlocking(false);
                        // client.register(selector, SelectionKey.OP_READ);
                    }
                } else if (key.isReadable()) {
                    System.out.println("readable");
                } else {
                    System.out.println("ikhan");
                }
            }
            selector.selectedKeys().clear();
        }

    }
}

package NioServer;

import DTO.Server;
import java.io.IOException;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NioServer {
    public void start(List<Server> serverConfig) throws IOException {
        Selector selector = Selector.open();

        Map<String, List<Server>> BindData = new HashMap<>();
        for (Server serv : serverConfig) {
            String host = serv.getHost();
            for (Object port : serv.getPort()) {
                String dns = host + ":" + port;
                BindData.computeIfAbsent(dns,k -> new ArrayList<>()).add(serv);
            }
        }
        System.out.println(BindData);
    }
}

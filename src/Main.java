import DTO.Server;
import Nio.NioServer;
import config.Parser;
import config.model.WebServerConfig;
import java.util.LinkedHashSet;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {

        Object config = new Parser("config.json").parse();
        System.out.println("Config file parsing...");
        List<Server> servers = new WebServerConfig((LinkedHashSet<Object>) config).setup();

        NioServer nioServer = new NioServer();
        nioServer.start(servers);
    }
}

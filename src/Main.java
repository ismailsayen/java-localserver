import config.Parser;
import config.model.WebServerConfig;
import server.Server;

import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {

        Object config = new Parser("config.json").parse();
        System.out.println("Config file parsing...");
        List<Server> ser=new WebServerConfig(config).setup();
    }
}

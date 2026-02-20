import config.Parser;
import config.model.WebServerConfig;
import java.util.LinkedHashSet;
import java.util.List;
import server.Server;

public class Main {
    public static void main(String[] args) throws Exception {

        Object config = new Parser("config.json").parse();
        System.out.println("Config file parsing...");
        List<Server> ser=new WebServerConfig((LinkedHashSet<Object>)config).setup();
    
        for (Server elem : ser) {
            System.out.println(elem.getName());
        }
    }
}

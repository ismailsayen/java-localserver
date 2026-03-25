import DTO.Server;
import Nio.NioServer;
import config.Parser;
import config.model.WebServerConfig;
import java.util.LinkedHashSet;
import java.util.List;

public class Main {

    private static final String GREEN = "\u001B[32m";
    private static final String RESET = "\u001B[0m";

    public static void main(String[] args) throws Exception {

        Object config = new Parser("config.json").parse();
        List<Server> servers = new WebServerConfig((LinkedHashSet<Object>) config).setup();

        printWelcome();
        printServerUrls(servers);

        NioServer nioServer = new NioServer();
        nioServer.start(servers);
    }

    private static void printWelcome() {
    System.out.println(
            GREEN +
            """
                         ██╗ █████╗ ██╗   ██╗ █████╗     ██╗      ██████╗  ██████╗ █████╗ ██╗     ███████╗███████╗██████╗ ██╗   ██╗███████╗██████╗
                         ██║██╔══██╗██║   ██║██╔══██╗    ██║     ██╔═══██╗██╔════╝██╔══██╗██║     ██╔════╝██╔════╝██╔══██╗██║   ██║██╔════╝██╔══██╗
                         ██║███████║██║   ██║███████║    ██║     ██║   ██║██║     ███████║██║     ███████╗█████╗  ██████╔╝██║   ██║█████╗  ██████╔╝
                    ██   ██║██╔══██║╚██╗ ██╔╝██╔══██║    ██║     ██║   ██║██║     ██╔══██║██║     ╚════██║██╔══╝  ██╔══██╗╚██╗ ██╔╝██╔══╝  ██╔══██╗
                    ╚█████╔╝██║  ██║ ╚████╔╝ ██║  ██║    ███████╗╚██████╔╝╚██████╗██║  ██║███████╗███████║███████╗██║  ██║ ╚████╔╝ ███████╗██║  ██║
                     ╚════╝ ╚═╝  ╚═╝  ╚═══╝  ╚═╝  ╚═╝    ╚══════╝ ╚═════╝  ╚═════╝╚═╝  ╚═╝╚══════╝╚══════╝╚══════╝╚═╝  ╚═╝  ╚═══╝  ╚══════╝╚═╝  ╚═╝

                                                            """ +
            RESET
    );
}

    private static void printServerUrls(List<Server> servers) {
        System.out.println("Server running on:\n");

        for (Server server : servers) {

            String host = server.getHost();
            String label = server.getDefaultServer() ? " (default)" : "";

            System.out.println("🌐 " + server.getName() + label);

            for (Object portObj : server.getPort()) {
                int port = Integer.parseInt(portObj.toString());
                System.out.println("   👉 http://" + host + ":" + port);
            }

            System.out.println();
        }
    }
}

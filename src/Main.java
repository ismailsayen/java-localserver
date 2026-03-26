import DTO.Server;
import Nio.NioServer;
import config.Parser;
import config.model.WebServerConfig;
import customError.FormatException;
import java.util.LinkedHashSet;
import java.util.List;

public class Main {

    private static final String GREEN = "\u001B[32m";
    private static final String RESET = "\u001B[0m";

    public static void main(String[] args) throws Exception {

        try {
            Object config = new Parser("config.json").parse();
            List<Server> servers = new WebServerConfig((LinkedHashSet<Object>) config).setup();
            if(servers.isEmpty()){
                System.out.println("No server to run.");
                return;
            }
            printWelcome();
            System.out.println();
            NioServer nioServer = new NioServer();
            nioServer.start(servers);
        } catch (FormatException e) {
            System.out.println(e.getMessage());
        }
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

                                                                                      """
                        +
                        RESET);
    }

   
}

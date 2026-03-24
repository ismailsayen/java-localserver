package handlers;

import java.nio.file.Path;
import java.util.Map;

import Nio.ClientHandler;

public class ErrorHandler {
    private ClientHandler client;


    public ErrorHandler(ClientHandler client) {
        this.client = client;
    }

    public void error(String code,String message){
        Map<String, String> errorPages=this.client.getVirtualHosts().getErrorPages();

        if(errorPages!=null){
            Path file=Path.of(errorPages.get(code));
            
            client.setIsResponseDone(true);
        }
    }
}

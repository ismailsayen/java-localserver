package handlers;

import Nio.ClientHandler;
import http.HttpHandler;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DeleteHandler implements HttpHandler {

    private final ClientHandler client;

    public DeleteHandler(ClientHandler client) {
        this.client = client;
    }

    @Override
    public void handle() throws Exception {
        String filePath = client.getHttpRequest().resolveFilePath();
        Path file = Path.of(filePath);
        if (!Files.exists(file)) {
            // error 404
            return;
        }

        if (Files.isDirectory(file) ) {
            // error 403
            System.out.println("lala ya waladi");
            return;
        }
        Boolean delete = file.toFile().delete();
        if(delete){
            System.out.println("ba77");
        }else{
            //eroor 500
        }
    }

    @Override
    public void response() throws IOException {
    }

}

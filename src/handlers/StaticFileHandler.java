package handlers;

import java.nio.file.Files;
import java.nio.file.Path;

import Nio.ClientHandler;
import http.HttpHandler;
import http.HttpRequest;
import java.io.File;

public class StaticFileHandler implements HttpHandler {

    @Override
    public void read() {
        // TODO Auto-generated method stub
        System.out.println("Unimplemented method 'read'  StaticFile");
    }

    @Override
    public void write() {
        System.out.println(" write static File");
    }

    @Override
    public void handle(HttpRequest request, ClientHandler client) throws Exception {
        String filePath = request.resolveFilePath();

        Path file = Path.of(filePath);

        if (!Files.exists(file)) {
            System.out.println("===>" + filePath);
            return;
        }
        System.out.println("+++++" + Files.isDirectory(file));
    }

}

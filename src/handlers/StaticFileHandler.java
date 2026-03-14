package handlers;

import DTO.Route;
import Nio.ClientHandler;
import http.HttpHandler;
import http.HttpRequest;
import java.nio.file.Files;
import java.nio.file.Path;

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
            throw new Exception("no Path:" + filePath);
        }
        if (Files.isDirectory(file)) {

            Route rt = request.getRoute();
            System.out.println(rt);
            if (rt.getIndex() != null) {
                Path indexFile = file.resolve(rt.getIndex());

                if (Files.exists(indexFile)) {
                    byte[] body = Files.readAllBytes(indexFile);
                    client.sendResponse(200, "text/html", body);
                    return;
                }
            }

            if (rt.getDirectoryListing()) {
                String html = generateDirectoryListing(file.toFile(), rt.getPath());
                client.sendResponse(200, "text/html", html.getBytes());
                return;
            }

            throw new Exception("Directory listing forbidden");

        }

        byte[] body = Files.readAllBytes(file);

        String contentType = Files.probeContentType(file);
        if (contentType == null)
            contentType = "application/octet-stream";

        client.sendResponse(200, contentType, body);
    }

private String generateDirectoryListing(java.io.File directory, String requestPath) {
    StringBuilder html = new StringBuilder();

    html.append("<html><body>");
    html.append("<h1>Index of ").append(requestPath).append("</h1>");

    java.io.File[] files = directory.listFiles();

    if (files != null) {
        for (java.io.File file : files) {

            String name = file.getName();
            String link = requestPath.endsWith("/") 
                    ? requestPath + name 
                    : requestPath + "/" + name;

            if (file.isDirectory()) {
                name += "/";
                link += "/";
            }

            html.append("<a href=\"")
                .append(link)
                .append("\">")
                .append(name)
                .append("</a><br>");
        }
    }

    html.append("</body></html>");

    return html.toString();
}

}

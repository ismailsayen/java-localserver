package handlers;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import Nio.ClientHandler;
import http.HttpHandler;

public class MultipartHandler implements HttpHandler {

    private final ClientHandler client;

    public MultipartHandler(ClientHandler client) {
        this.client = client;
    }

    @Override
    public void handle() throws Exception {
        this.handleMultipartFields();
        this.client.setIsResponseDone(true);
        this.client.getKey().interestOps(SelectionKey.OP_WRITE);
    }

    @Override
    public void response() throws IOException {

    }

    private void saveFile(byte[] file, String fileName) throws IOException {
        String root = this.client.getHttpRequest().getRoute().getRoot() + "/assets";

        Path directory = Paths.get(root);

        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }

        String baseName = fileName;
        String extension = "";

        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex != -1) {
            baseName = fileName.substring(0, dotIndex);
            extension = fileName.substring(dotIndex);
        }

        Path filePath = directory.resolve(fileName);

        int counter = 1;

        while (Files.exists(filePath)) {
            String newName = baseName + "_" + counter + extension;
            filePath = directory.resolve(newName);
            counter++;
        }

        Files.write(filePath, file);

        System.out.println("File created succesfully: " + filePath.getFileName().toString());
    }

    private void handleMultipartFields() throws IOException {
        String contentType = this.client.getHttpHeader().getHeaders().get("content-type");
        if (!contentType.contains("boundary=")) {
            throw new RuntimeException("multipart/form-data should contain boundary");
        }

        String[] parts = contentType.split("boundary=");
        if (parts.length < 2) {
            throw new RuntimeException("boundary should contain a value");
        }

        String boundary = parts[1].trim();
        if (boundary.startsWith("\"") && boundary.endsWith("\"")) {
            boundary = boundary.substring(1, boundary.length() - 1);
        }

        byte[] boundaryBytes = ("--" + boundary).getBytes(StandardCharsets.UTF_8);
        byte[] body = this.client.getHttpRequest().getBody();

        int start = indexOf(body, boundaryBytes, 0);
        while (start != -1) {
            int next = indexOf(body, boundaryBytes, start + boundaryBytes.length);

            if (next == -1) {
                break;
            }

            byte[] part = Arrays.copyOfRange(body, start + boundaryBytes.length + 2, next);

            this.handlePart(part);

            start = next;
        }
    }

    private int indexOf(byte[] data, byte[] pattern, int start) {
        for (int i = start; i <= data.length - pattern.length; i++) {
            boolean found = true;

            for (int j = 0; j < pattern.length; j++) {
                if (data[i + j] != pattern[j]) {
                    found = false;
                    break;
                }
            }

            if (found) {
                return i;
            }
        }
        return -1;
    }

    private void handlePart(byte[] part) throws IOException {
        byte[] pattern = "\r\n\r\n".getBytes(StandardCharsets.UTF_8);

        int headersEnd = this.indexOf(part, pattern, 0);
        byte[] headersBytes = Arrays.copyOfRange(part, 0, headersEnd);
        String headers = new String(headersBytes, StandardCharsets.UTF_8);

        String fileName = this.extractFileName(headers);
        if (fileName == null) {
            return;
        }

        byte[] fileBytes = Arrays.copyOfRange(part, headersEnd + 4, part.length - 2);
        this.saveFile(fileBytes, fileName);
    }

    private String extractFileName(String headers) {
        int start = headers.indexOf("filename=\"");
        if (start == -1) {
            return null;
        }

        start += 10;

        int end = headers.indexOf("\"", start);

        return headers.substring(start, end);
    }

}

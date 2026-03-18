package handlers;

import java.io.*;
import java.nio.channels.SelectionKey;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import Nio.ClientHandler;
import http.HttpHandler;

public class MultipartHandler implements HttpHandler {

    private final ClientHandler client;
    private static final int BUFFER_SIZE = 16384;
    private static final String HEADERS_END = "\r\n\r\n";

    public MultipartHandler(ClientHandler client) {
        this.client = client;
    }

    @Override
    public void handle() throws Exception {
        processMultipartBody();
        this.client.setIsResponseDone(true);
        client.getKey().interestOps(SelectionKey.OP_WRITE);
    }

    @Override
    public void response() throws IOException {
        
    }

    private void processMultipartBody() throws IOException {
        String contentType = this.client.getHttpHeader().getHeaders().get("content-type");
        String boundary = extractBoundary(contentType);
        byte[] boundaryBytes = ("\r\n--" + boundary).getBytes(StandardCharsets.UTF_8);

        Path bodyPath = Paths.get("body.tmp");

        try (BufferedInputStream bis = new BufferedInputStream(Files.newInputStream(bodyPath))) {
            byte[] buffer = new byte[BUFFER_SIZE];
            ByteArrayOutputStream window = new ByteArrayOutputStream();
            FileOutputStream currentFile = null;
            boolean readingHeaders = true;
            int bytesRead;

            while ((bytesRead = bis.read(buffer)) != -1) {
                window.write(buffer, 0, bytesRead);
                byte[] data = window.toByteArray();
                window.reset();

                int pos = 0;
                while (pos < data.length) {
                    if (readingHeaders) {
                        int headersEndIndex = indexOf(data, HEADERS_END.getBytes(), pos);
                        if (headersEndIndex != -1) {
                            String headers = new String(data, pos, headersEndIndex - pos, StandardCharsets.UTF_8);
                            String fileName = extractFileName(headers);

                            if (fileName != null) {
                                currentFile = createUniqueFile(fileName);
                            }

                            pos = headersEndIndex + HEADERS_END.length();
                            readingHeaders = false;
                        } else {
                            window.write(data, pos, data.length - pos);
                            break;
                        }
                    } else {
                        int nextBoundaryIndex = indexOf(data, boundaryBytes, pos);

                        if (nextBoundaryIndex != -1) {
                            if (currentFile != null) {
                                currentFile.write(data, pos, nextBoundaryIndex - pos);
                                currentFile.close();
                                currentFile = null;
                            }
                            pos = nextBoundaryIndex + boundaryBytes.length;
                            readingHeaders = true;
                        } else {
                            int safeWriteLen = (data.length - pos) - boundaryBytes.length;
                            if (safeWriteLen > 0) {
                                if (currentFile != null) {
                                    currentFile.write(data, pos, safeWriteLen);
                                }
                                pos += safeWriteLen;
                            }
                            window.write(data, pos, data.length - pos);
                            break;
                        }
                    }
                }
            }
            if (currentFile != null)
                currentFile.close();
        } finally {
            Files.deleteIfExists(bodyPath);
        }
    }

    private String extractBoundary(String contentType) {
        if (!contentType.contains("boundary=")) {
            throw new RuntimeException("Missing boundary in Content-Type");
        }
        String boundary = contentType.split("boundary=")[1].trim();
        return (boundary.startsWith("\"") && boundary.endsWith("\""))
                ? boundary.substring(1, boundary.length() - 1)
                : boundary;
    }

    private FileOutputStream createUniqueFile(String fileName) throws IOException {
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
            filePath = directory.resolve(baseName + "_" + counter + extension);
            counter++;
        }

        System.out.println("Saving file to: " + filePath);
        return new FileOutputStream(filePath.toFile());
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
            if (found)
                return i;
        }
        return -1;
    }

    private String extractFileName(String headers) {
        int start = headers.indexOf("filename=\"");
        if (start == -1)
            return null;
        start += 10;
        int end = headers.indexOf("\"", start);
        return headers.substring(start, end);
    }
}
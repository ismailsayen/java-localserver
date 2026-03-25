package handlers;

import Nio.ClientHandler;
import config.utils.Session;
import http.HttpHandler;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UploadsHandler implements HttpHandler {

    private final ClientHandler client;
    private List<String> filesName = new ArrayList<>();
    private static final String HEADERS_END = "\r\n\r\n";

    private ReadingState readingState = ReadingState.READING_HEADERS;
    private FileOutputStream currentFile = null;
    private final ByteArrayOutputStream window = new ByteArrayOutputStream();
    private byte[] boundaryBytes;
    private byte[] finalBoundaryBytes;
    private boolean isStartProcessing = false;

    private enum ReadingState {
        READING_HEADERS, READING_BODY, DONE
    }

    public UploadsHandler(ClientHandler client) {
        this.client = client;
    }

    @Override
    public void handle() throws Exception {
        processMultipartBody(this.client.getByteArrayOutputStream().toByteArray());
        if (this.client.getIsRequestDone()) {
            this.client.setIsResponseDone(true);
            client.getKey().interestOps(SelectionKey.OP_WRITE);
        }
    }

    @Override
    public void response() throws IOException {
        String body = String.join(", ", filesName);
        byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);
        Session session = client.getHttpRequest().getSession();

        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 200 OK\r\n");
        sb.append("Content-Type: text/plain; charset=UTF-8\r\n");
        sb.append("Set-Cookie: SESSION_ID=" + session.getId() + "; Path=/\r\n");
        sb.append("Content-Length: ").append(bodyBytes.length).append("\r\n");
        sb.append("Connection: close\r\n");
        sb.append("\r\n");

        byte[] headerBytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.allocate(headerBytes.length + bodyBytes.length);

        buffer.put(headerBytes);
        buffer.put(bodyBytes);

        buffer.flip();

        while (buffer.hasRemaining()) {
            this.client.getClient().write(buffer);
        }
    }

    public void processMultipartBody(byte[] chunk) throws IOException {
        if (!isStartProcessing) {
            this.filesName.clear();
            String contentType = this.client.getHttpHeader().getHeaders().get("content-type");
            String boundary = extractBoundary(contentType);
            this.boundaryBytes = ("\r\n--" + boundary).getBytes(StandardCharsets.UTF_8);
            this.finalBoundaryBytes = ("--" + boundary + "--").getBytes(StandardCharsets.UTF_8);
            this.isStartProcessing = true;
        }

        window.write(chunk, 0, chunk.length);
        byte[] data = window.toByteArray();
        window.reset();

        int pos = 0;
        while (pos < data.length) {
            int finalBoundaryIdx = indexOf(data, finalBoundaryBytes, pos);
            if (finalBoundaryIdx != -1) {
                if (currentFile != null) {
                    currentFile.write(data, pos, finalBoundaryIdx - pos);
                    currentFile.close();
                    currentFile = null;
                }
                this.readingState = ReadingState.DONE;
                return;
            }

            if (this.readingState == ReadingState.READING_HEADERS) {
                int headersEndIndex = indexOf(data, HEADERS_END.getBytes(), pos);
                if (headersEndIndex != -1) {
                    String headers = new String(data, pos, headersEndIndex - pos, StandardCharsets.UTF_8);
                    String fileName = extractFileName(headers);

                    if (fileName != null) {
                        this.currentFile = createUniqueFile(fileName);
                    }

                    pos = headersEndIndex + HEADERS_END.length();
                    this.readingState = ReadingState.READING_BODY;
                } else {
                    window.write(data, pos, data.length - pos);
                    break;
                }
            } else if (this.readingState == ReadingState.READING_BODY) {
                int nextBoundaryIndex = indexOf(data, boundaryBytes, pos);

                if (nextBoundaryIndex != -1) {
                    if (currentFile != null) {
                        currentFile.write(data, pos, nextBoundaryIndex - pos);
                        currentFile.close();
                        currentFile = null;
                    }
                    pos = nextBoundaryIndex + boundaryBytes.length;
                    this.readingState = ReadingState.READING_HEADERS;
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
        String root = this.client.getHttpRequest().getRoute().getRoot();
        Path directory = Paths.get(root);

        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }

        fileName = fileName.replaceAll("\\s+", "_");

        String baseName = fileName;
        String extension = "";
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex != -1) {
            baseName = fileName.substring(0, dotIndex);
            extension = fileName.substring(dotIndex);
        }

        Path filePath = directory.resolve(fileName);
        String newFileName = baseName + extension;
        if (Files.exists(filePath)) {
            String uniqueId = UUID.randomUUID().toString();
            filePath = directory.resolve(baseName + "_" + uniqueId + extension);
            newFileName = baseName + "_" + uniqueId + extension;
        }

        this.filesName.add(newFileName);
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
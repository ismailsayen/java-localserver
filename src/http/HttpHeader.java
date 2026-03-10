package http;

import java.util.HashMap;
import java.util.Map;

public class HttpHeader {
    private String method;
    private String path;
    private String protocol;
    private Map<String, String> headers;

    public static HttpHeader parseHeaders(String reqHead) {
        String[] request = reqHead.split("\r\n");

        // get Request_line
        String[] reqLine = request[0].split("\s");

        HttpHeader httpHeader = new HttpHeader();

        httpHeader.setMethod(reqLine[0]);
        httpHeader.setPath(reqLine[1]);
        httpHeader.setProtocol(reqLine[2]);
        Map<String, String> map = new HashMap<>();
        for (int i = 1; i < request.length; i++) {
            String[] line = request[i].split(":", 2);
            String key = line[0].trim().toLowerCase();
            String val = line[1].trim().toLowerCase();
            map.put(key, val);
        }
        httpHeader.setHeaders(map);
        return httpHeader;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    @Override
    public String toString() {
        return String.format("Method: %s | Path: %s | Protocol: %s", method, path, protocol);
    }
}

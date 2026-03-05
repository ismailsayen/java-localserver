package http;

import java.util.HashMap;
import java.util.Map;

public class HttpHeader {
    private String method;
    private String path;
    private String version;
    private Map<String, String> headers;

    public HttpHeader parseHeaders(String reqHead) {
        String[] request = reqHead.split("\r\n");

        HttpHeader obj = new HttpHeader();

        // get Request_line
        String[] reqLine = request[0].split("\s");

        obj.setMethod(reqLine[0]);
        obj.setPath(reqLine[1]);
        obj.setVersion(reqLine[2]);
        Map<String, String> map = new HashMap<>();
        for (int i = 1; i < request.length; i++) {
            String[] line = request[i].split(":",2);
            String key = line[0].trim().toLowerCase();
            String val = line[1].trim().toLowerCase();
            map.put(key, val);
        }
        obj.setHeaders(map);
        return obj;
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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
}

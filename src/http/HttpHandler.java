package http;

import java.io.IOException;

public interface HttpHandler {
    void handle() throws Exception;
    void response()  throws IOException;
}

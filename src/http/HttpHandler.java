package http;

public interface HttpHandler {
    public void read();

    public void write();

    public HttpResponse handel();
}

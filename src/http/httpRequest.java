package http;

public class HttpRequest {
    public enum Request_Status {
        READY,
        PROCESSING,
        METHOD_NOT_ALLOWED,
        ERROR
    }

    private HttpHeader httpHeader;
    private Request_Status status;
    private int contentLength = 0;

    public HttpRequest(HttpHeader httpHeader) {
        this.httpHeader = httpHeader;
        String method = httpHeader.getMethod().toUpperCase();

        switch (method) {
            case "GET", "DELETE":
                this.status = Request_Status.READY;
                break;

            case "POST":
                validatePayloadMethod();
                break;

            default:
                this.status = Request_Status.METHOD_NOT_ALLOWED;
        }
    }

    private void validatePayloadMethod() {
        String cl = httpHeader.getHeaders().get("content-length");
        String te = httpHeader.getHeaders().get("transfer-encoding");

        if (cl == null && te == null) {
            // Un POST/PUT sans indication de taille est une erreur 411 Length Required
            this.status = Request_Status.ERROR;
        } else if (cl != null) {
            this.contentLength = Integer.parseInt(cl);
            // Si Content-Length est 0, on n'attend rien, donc c'est READY
            this.status = (this.contentLength == 0) ? Request_Status.READY : Request_Status.PROCESSING;
        } else {
            // C'est du chunked encoding
            this.status = Request_Status.PROCESSING;
        }
    }

    public int getContentLength() {
        return contentLength;
    }

    public Request_Status getStatus() {
        return status;
    }

    public void setStatus(Request_Status status) {
        this.status = status;
    }
}

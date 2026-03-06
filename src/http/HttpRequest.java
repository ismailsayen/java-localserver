package http;

public class HttpRequest {

    private HttpHeader httpHeader;
    private RequestStatus status;
    private int contentLength = 0;

    public HttpRequest(HttpHeader httpHeader) {
        this.httpHeader = httpHeader;
        String method = httpHeader.getMethod().toUpperCase();

        switch (method) {
            case "GET", "DELETE":
                this.status = RequestStatus.READY;
                break;

            case "POST":
                validatePayloadMethod();
                break;

            default:
                this.status = RequestStatus.METHOD_NOT_ALLOWED;
        }
    }

    private void validatePayloadMethod() {
        String cl = httpHeader.getHeaders().get("content-length");
        String te = httpHeader.getHeaders().get("transfer-encoding");
        
        if (cl == null && te == null) {
            this.status = RequestStatus.ERROR;
        } else if (cl != null) {
            this.contentLength = Integer.parseInt(cl);
            this.status = (this.contentLength == 0) ? RequestStatus.READY : RequestStatus.PROCESSING;
        } else {
            this.status = RequestStatus.PROCESSING;
        }
    }

    public int getContentLength() {
        return contentLength;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }
}

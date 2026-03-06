package http;

public class HttpRequest {

    private HttpHeader httpHeader;
    private RequestStatus status;
    private int contentLength = 0;
    private boolean chnked = false;
    private Boolean isMultipart = false;
    private String boundary;

    public HttpRequest(HttpHeader httpHeader) {
        this.httpHeader = httpHeader;
        String method = httpHeader.getMethod().toUpperCase();

        switch (method) {
            case "GET", "DELETE" -> validatePayloadMethod();

            case "POST" -> validatePayloadMethod();

            default -> this.status = RequestStatus.METHOD_NOT_ALLOWED;
        }
    }

    private void validatePayloadMethod() {
        String cl = httpHeader.getHeaders().get("content-length");
        String te = httpHeader.getHeaders().get("transfer-encoding");

        if (cl == null && te == null) {

            this.status = (httpHeader.getMethod().toUpperCase().equals("POST")) ? RequestStatus.ERROR
                    : RequestStatus.PROCESSING;
        } else if (cl != null) {
            this.contentLength = Integer.parseInt(cl);
            this.status = (this.contentLength == 0) ? RequestStatus.READY : RequestStatus.PROCESSING;
        } else {
            this.chnked = true;
            this.status = RequestStatus.PROCESSING;
        }
        extractMultipartDetails();
    }

    private void extractMultipartDetails() {
        String contentType = this.httpHeader.getHeaders().get("content-type");
        if (contentType != null && contentType.contains("multipart/form-data")) {
            if (contentType.contains("boundary=")) {
                this.isMultipart = true;
                String[] parts = contentType.split("boundary=");
                if (parts.length > 1) {
                    this.boundary = parts[1].trim();
                    if (this.boundary.startsWith("\"") && this.boundary.endsWith("\"")) {
                        this.boundary = this.boundary.substring(1, this.boundary.length() - 1);
                    }
                }
            }
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

    public boolean isChnked() {
        return chnked;
    }

    public void setChnked(boolean chnked) {
        this.chnked = chnked;
    }

    public Boolean getIsMultipart() {
        return isMultipart;
    }

    public void setIsMultipart(Boolean isMultipart) {
        this.isMultipart = isMultipart;
    }

    public String getBoundary() {
        return this.boundary;
    }
}

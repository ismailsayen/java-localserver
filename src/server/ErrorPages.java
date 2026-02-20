package server;

public class ErrorPages {
    private String status;
    private String path;

    public ErrorPages( String status, String path){
        this.status=status;
        this.path=path;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
    
}

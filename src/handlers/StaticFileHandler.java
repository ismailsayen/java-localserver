package handlers;

import http.HttpHandler;
import http.HttpResponse;

public class StaticFileHandler implements HttpHandler {

    @Override
    public void read() {
        // TODO Auto-generated method stub
       System.out.println("Unimplemented method 'read'  StaticFile");
    }

    @Override
    public void write() {
        System.out.println(" write static File");
    }

    @Override
    public HttpResponse handel() {
        // TODO Auto-generated method stub
       System.out.println("Unimplemented method 'handel'  StaticFile");
       return null;

    }
    
}

package handlers;

import http.HttpHandler;
import http.HttpResponse;

public class CgiHandler implements HttpHandler {

    @Override
    public void read() {
        // TODO Auto-generated method stub
       System.out.println("Unimplemented method 'read' cgi");
    }

    @Override
    public void write() {
        // TODO Auto-generated method stub
       System.out.println("Unimplemented method 'write' cgi");
    }

    @Override
    public HttpResponse handel() {
        // TODO Auto-generated method stub
       System.out.println("Unimplemented method 'handel' cgi");
       return null;
    }

}

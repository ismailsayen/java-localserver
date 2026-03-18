package DTO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

public class Route {
    private String path; // route path (/ , /images)
    private String root; // directory root
    private List<String> methods; // accepted HTTP methods
    private String index; // default file for directories
    private Boolean directoryListing; // enable/disable directory listing
    private String redirectTo; // redirection target
    private Long redirectStatusCode; // redirection status code
    private String cgiExtension; // CGI by file extension

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    public List<String> getMethods() {
        return methods;
    }

    public void setMethods(LinkedHashSet<String> methods) {
        String[] allowedMethods = { "GET", "POST", "DELETE" };
        List<String> newList = new ArrayList<>();

        for (String method : methods) {
            if (Arrays.asList(allowedMethods).contains(method)) {
                newList.add(method);
            }
        }

        this.methods = newList;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public Boolean getDirectoryListing() {
        return directoryListing;
    }

    public void setDirectoryListing(Boolean directoryListing) {
        this.directoryListing = directoryListing;
    }

    public String getRedirectTo() {
        return redirectTo;
    }

    public void setRedirectTo(String redirectTo) {
        this.redirectTo = redirectTo;
    }

    public Long getRedirectStatusCode() {
        return redirectStatusCode;
    }

    public void setRedirectStatusCode(Long redirectStatusCode) {
        this.redirectStatusCode = redirectStatusCode;
    }

    public String getCgi() {
        return cgiExtension;
    }

    public void setCgi(String cgi) {
        this.cgiExtension = cgi;
    }

    @Override
    public String toString() {
        return """
                Route {
                  path='""" + path + '\'' +
                ",\n  root='" + root + '\'' +
                ",\n  methods=" + methods +
                ",\n  index='" + index + '\'' +
                ",\n  directoryListing=" + directoryListing +
                ",\n  redirectTo='" + redirectTo + '\'' +
                ",\n  redirectStatusCode=" + redirectStatusCode +
                ",\n  cgiExtension=" + cgiExtension +
                "\n}";
    }

}

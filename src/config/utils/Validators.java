package config.utils;

import DTO.Route;
import DTO.Server;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.regex.Pattern;

public class Validators {

    public static Server ServerValidator(Map<String, Object> ser) {

        Server server = new Server();

        // host
        if (!(ser.get("host") instanceof String)) {
            return null;
        }

        String host = (String) ser.get("host");

        Pattern pattern = Pattern.compile(
                "^((25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])\\.){3}(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])$");

        if (!pattern.matcher(host).matches()) {
            return null;
        }

        server.setHost(host);

        // port
        if (!(ser.get("port") instanceof LinkedHashSet<?>)) {
            return null;
        }

        LinkedHashSet<Object> ports = (LinkedHashSet<Object>) ser.get("port");

        Iterator<Object> it = ports.iterator();
        while (it.hasNext()) {
            Object obj = it.next();

            if (!(obj instanceof Long)) {
                return null;
            }

            Long p = (Long) obj;
            if (p < 1024 || p > 65535) {
                it.remove();
            }
        }

        if (ports.isEmpty()) {
            return null;
        }

        server.setPort(ports);

        // name
        if (ser.get("name") != null && !(ser.get("name") instanceof String)) {
            return null;
        }

        server.setName((String) ser.get("name"));

        // defaultServer
        server.setDefaultServer(
                (Boolean) ser.getOrDefault("defaultServer", false));

        // limitRequestBody
        server.setLimitRequestBody(
                (Long) ser.getOrDefault("limitRequestBody", 20000000L));

        // errorPages
        if (ser.get("errorPages") instanceof Map) {
            server.setErrorPages((Map<String, String>) ser.get("errorPages"));
        }
        return server;
    }

    public static Route routeValidator(Map<String, Object> rt) {
        Route route = new Route();

        // ===== path (obligatoire) =====
        if (rt.get("path") != null) {
            route.setPath((String) rt.get("path"));
        }

        // ===== root =====
        if (rt.get("root") != null) {

            route.setRoot((String) rt.get("root"));
        }

        // ===== methods =====
        if (rt.get("methods") != null) {
            route.setMethods((LinkedHashSet<String>) rt.get("methods"));

        }

        // ===== index =====
        if (rt.get("index") != null) {

            route.setIndex((String) rt.get("index"));
        }

        // ===== directoryListing =====
            route.setDirectoryListing((Boolean) rt.getOrDefault("directoryListing",false));
        
        // ===== redirectTo =====
        if (rt.get("redirectTo") != null) {

            route.setRedirectTo((String) rt.get("redirectTo"));
        }

        // ===== redirectStatusCode =====
        if (rt.get("redirectStatusCode") != null) {

            route.setRedirectStatusCode((Long) rt.get("redirectStatusCode"));
        }

        // ===== cgi =====
        if (rt.get("cgiExtension") != null) {

            route.setCgi((String) rt.get("cgiExtension"));
        }

        return route;
    }

}

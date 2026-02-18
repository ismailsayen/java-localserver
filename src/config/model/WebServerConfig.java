package config.model;

import server.Server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WebServerConfig {
    private Object servers;

    public WebServerConfig(Object servers){
        this.servers=servers;
    }

    //[{path=/, methods=[GET, POST]}], port=8080, host=127.0.0.1, name=server1, default_server=true}]
    public List<Server> setup(){
        List<Server> servers=new ArrayList<>();
       
        return servers;
    }
}

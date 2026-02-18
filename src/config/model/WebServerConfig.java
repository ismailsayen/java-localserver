package config.model;

import server.Server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WebServerConfig {
    private List<Map<String,Object>> servers;

    public WebServerConfig(Object servers){
        this.servers=(List<Map<String,Object>>)servers;
    }

    //[{path=/, methods=[GET, POST]}], port=8080, host=127.0.0.1, name=server1, default_server=true}]
    public List<Server> setup(){
        List<Server> servers=new ArrayList<>();
        for (Map<String,Object> server:this.servers){
            if(server.get("routes") instanceof List){
                System.out.println("sssssss");
            }
        }
        return servers;
    }
}

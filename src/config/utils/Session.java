package config.utils;

import java.util.Map;
import java.util.HashMap;

public class Session {
    private String id;
    private Map<String, Object> data = new HashMap<>();
    private long lastAccessTime;

    public Session(String id) {
        this.id = id;
        this.lastAccessTime = System.currentTimeMillis();
    }

    public String getId() {
        return this.id;
    }

    public Map<String, Object> getData() {
        return this.data;
    }

    public long getLastAccessTime() {
        return this.lastAccessTime;
    }

    public void touch() {
        this.lastAccessTime = lastAccessTime;
    }
}

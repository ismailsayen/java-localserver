package config.utils;

import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

public class SessionManager {
    private static final Map<String, Session> sessions = new HashMap<>();

    public static Session getSession(String sessionId) {
        return sessions.get(sessionId);
    }

    public static Session createSession() {
        String id = UUID.randomUUID().toString();
        Session session = new Session(id);
        sessions.put(id, session);
        return session;
    }
}

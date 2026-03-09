package http;

import java.lang.reflect.Method;

public enum RequestStatus {
    READY,
    PROCESSING,
    METHOD_NOT_ALLOWED,
    NOT_FOUND,
    ERROR
}

package http;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class httpParser {
    private SocketChannel client;
    private ByteBuffer buffer;

    public httpParser(SocketChannel client) {
        this.client = client;
        this.buffer = ByteBuffer.allocate(1024);
    }

    public SocketChannel getClient() {
        return client;
    }

    public void setClient(SocketChannel client) {
        this.client = client;
    }

}

package Handlers;

import java.nio.channels.SelectionKey;

// provides establishing the connection to the remote channel
public interface Connector {
    void connectToChannel(SelectionKey key) throws Exception;
}

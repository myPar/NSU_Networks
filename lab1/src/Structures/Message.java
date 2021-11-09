package Structures;

import java.net.InetAddress;

// class represents message consisted of app launch information (host address and id)
public class Message {
    // TODO refactor field host address: set string representation
    // ip address of host where app is working
    private String hostAddress;
    // app launch id - unique for different app launch on the same host
    private long launchId;
// constructor (for output message)
    public Message(String address, long id) {
        hostAddress = address;
        launchId = id;
    }
// getters:
    // get id value
    public long getLaunchId() {
        return launchId;
    }
    // get host address value
    public String getHostAddress() {
        return hostAddress;
    }
    // get host address + id string (get host key)
    public String getHostKey() {
        return hostAddress + launchId;
    }
}

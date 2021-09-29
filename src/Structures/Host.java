package Structures;

import java.net.InetAddress;

public class Host {
    // time counter field; need to check is host 'online'
    private long timeCounter;
    // host address (ipv4/ipv6)
    private InetAddress hostAddress;
    // unique id
    private long id;
// constructor
    public Host(InetAddress address, long id) {
        hostAddress = address;
        // init id as current time in millis
        this.id = id;
        // init time counter
        timeCounter = System.currentTimeMillis();
    }
// set counter value method
    public void setTimeCounter(long count) {
        timeCounter = count;
    }
// getters:
    // returns text representation of host address
    public String getHostAddress() {
        return hostAddress.getHostAddress();
    }
    // get host id
    public long getId() {
        return id;
    }
    // get time counter
    public long getTimeCounter() {
        return timeCounter;
    }
}

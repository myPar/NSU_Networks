package Structures;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Host {
    // time counter field; need to check is host 'online'
    private long timeCounter;
    // host address (ipv4/ipv6)
    private InetAddress hostAddress;
    // unique id
    private long id;

    // constructor
    public Host(String address) {
        try {
            // try to init host address
            hostAddress = InetAddress.getByName(address);
        } catch (UnknownHostException e) {
            System.err.println("Invalid host address:");
            e.printStackTrace();

            System.exit(1);
        }
        // init id as current time in millis
        id = System.currentTimeMillis();
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

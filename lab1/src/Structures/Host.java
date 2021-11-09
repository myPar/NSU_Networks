package Structures;

class Host {
    // time counter field; need to check is host 'online'
    private long timeCounter;
    // host address (ipv4/ipv6)
    private String hostAddress;
    // unique id
    private long id;
// constructor
    Host(String address, long id) {
        hostAddress = address;
        // init id as current time in millis
        this.id = id;
        // init time counter
        timeCounter = System.currentTimeMillis();
    }
// set counter value method
    void setTimeCounter(long count) {
        timeCounter = count;
    }
// getters:
    // returns text representation of host address
    String getHostAddress() {
        return hostAddress;
    }
    // get host id
    long getId() {
        return id;
    }
    // get time counter
    long getTimeCounter() {
        return timeCounter;
    }
}

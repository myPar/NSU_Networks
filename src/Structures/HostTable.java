package Structures;

import java.util.HashMap;

// this structure represents thread save set of hosts in group
public class HostTable {
    // map of hosts: key - host Name (Address + id); value - Host
    private HashMap<String, Host> hostMap;
    // host keep alive time
    private static long maxResponseTime;

    // config keep alive time method
    public static void config(long time) {
        maxResponseTime = time;
    }
    // adding new host to the table
    synchronized void addHost(String address, long id) {
        // create host
        Host host = new Host(address, id);
        // create key
        String key = host.getHostAddress() + host.getId();
        // add host to table
        hostMap.put(key, host);
    }
    // checking time counter for all hosts in the table; removing hosts with time counter exceeded
    public synchronized void checkAllHostsTimeCounter() {
        hostMap.forEach((key, value) -> {
            if (System.currentTimeMillis() - value.getTimeCounter() > maxResponseTime) {
                // response time exceeded - host disconnected. remove it from table
                hostMap.remove(key);
                System.out.println(value.getHostAddress() + " " + value.getId() + ": disconnected");
            }
        });
    }
    // check does table contains host with specified name
    synchronized boolean contains(String hostName) {
        return hostMap.containsKey(hostName);
    }
}

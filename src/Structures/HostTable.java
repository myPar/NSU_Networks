package Structures;

import java.util.HashMap;

// this structure represents thread save set of hosts in group
public class HostTable {
    // map of hosts: key - host Name (Address + id); value - Host
    private HashMap<String, Host> hostMap;
    // host keep alive time
    private long maxResponseTime;
    // View to update host table data
    private View view;
// constructor
    public HostTable(View view) {
        hostMap = new HashMap<>();
        this.view = view;
    }
    // config keep alive time method
    public void config(long time) {
        maxResponseTime = time;
    }
    // adding new host to the table
    public synchronized void addHost(String address, long id) {
        // create host
        Host host = new Host(address, id);
        // create key
        String key = host.getHostAddress() + host.getId();
        // add host to table
        hostMap.put(key, host);
        // display new host
        view.displayHostAdd(address, id);
    }
    // update time counter in host
    public synchronized  void updateHost(String key) {
        assert hostMap.containsKey(key);
        hostMap.get(key).setTimeCounter(System.currentTimeMillis());
    }
    // checking time counter for all hosts in the table; removing hosts with time counter exceeded
    public synchronized void checkAllHostsTimeCounter() {
        hostMap.forEach((key, value) -> {
            if (System.currentTimeMillis() - value.getTimeCounter() > maxResponseTime) {
                // response time exceeded - host disconnected. remove it from table
                hostMap.remove(key);
                //System.out.println(value.getHostAddress() + " " + value.getId() + ": disconnected");
                // display removing
                view.displayHostRemove(key);
            }
        });
    }
    // check does table contains host with specified name
    public synchronized boolean contains(String hostName) {
        return hostMap.containsKey(hostName);
    }
}

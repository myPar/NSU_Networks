package ThreadParts;

import Structures.HostTable;

public class TimeChecker extends Thread {
    // delta time of checking hosts time counts
    private int checkDeltaTime;
    // table of hosts
    private HostTable table;
// constructor
    public TimeChecker(HostTable t, int delta) {
        table = t;
        checkDeltaTime = delta;
    }
    @Override
    // main run method
    public void run() {
        // check time counter for all hosts every 'checkDeltaTime' period of time
        while(true) {
            table.checkAllHostsTimeCounter();
            try {
                Thread.sleep(checkDeltaTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }
}

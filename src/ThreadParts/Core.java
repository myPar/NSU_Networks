package ThreadParts;

import Structures.MySocket;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Core {
    // TODO: process receiving messages from itself
    private final int port = 6060;
    private InetAddress selfAddress;
    private long id;
// constructor
    public Core() {
        try {
            selfAddress = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            System.out.println("Fatal: can't get local host address");
            System.exit(1);
        }
        id = System.currentTimeMillis();
    }
    public void execute(String gAddress) {
        InetAddress groupAddress = null;
        try {
            // check address
            groupAddress = InetAddress.getByName(gAddress);
            // check is address - a multi-cast address
            if(!groupAddress.isMulticastAddress()) {
                throw new UnknownHostException();
            }
        } catch (UnknownHostException e) {
            System.out.println("Fatal: Invalid multi-cast group IP address");
            System.exit(1);
        }
        MySocket socket;
        // try to create socket
        try {
            socket = new MySocket(groupAddress, port);
        }
        catch (MySocket.SocketException e) {
            e.printException();
            System.out.println("Fatal: socket creation exception");
            System.exit(1);
        }
        // main execution loop
        while(true) {

        }
    }
}

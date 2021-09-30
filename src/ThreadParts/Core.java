package ThreadParts;

import Structures.HostTable;
import Structures.Message;
import Structures.MySocket;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Core {
    // TODO: refactor TimeChecker to nested class
    // app port
    private int port;
    // local host address
    private InetAddress selfAddress;
    // host id
    private long id;
    // delta time of packets sending
    private final int sendDeltaTime = 1;
    // socket
    private MySocket socket;
    // host table
    private HostTable table;
// core components:
    // sender
    private Sender sender;
    // receiver
    private Receiver receiver;

    // nested class Sender; sends messages in the separate thread to multi-cast group every 'sendDeltaTime' period
    private class Sender extends Thread {
        @Override
        public void run() {
            assert socket != null;
            while(true) {
                try {
                    socket.sendMessage(new Message(selfAddress.getHostAddress(), id));
                } catch (MySocket.SocketException e) {
                    // not fatal exception while packet sending
                    e.printException();
                }
                try {
                    Thread.sleep(sendDeltaTime);
                } catch (InterruptedException e) {
                    System.err.println("Fatal: InterruptedException");
                    System.exit(1);
                }
            }
        }
    }
// nested class Receiver; receives messages in the separate thread from multi-cast group
    private class Receiver extends Thread {
        // TODO: write run method:
        //  1.receive message
        //  2. check is message youth
        //  3. if no: add/update host
        @Override
        public void run() {
            assert socket != null;
        }
    }

// constructor
    public Core(int port) {
        // init port
        this.port = port;
        try {
            // get local host address
            selfAddress = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            System.err.println("Fatal: can't get local host address");
            System.exit(1);
        }
        // generate id
        id = System.currentTimeMillis();
        // create empty host table
        table = new HostTable();
        // init Core components:
        sender = new Sender();
        receiver = new Receiver();
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
            System.err.println("Fatal: Invalid multi-cast group IP address");
            System.exit(1);
        }
        // try to create socket
        try {
            socket = new MySocket(groupAddress, port);
        }
        catch (MySocket.SocketException e) {
            e.printException();
            System.err.println("Fatal: socket creation exception");
            System.exit(1);
        }

    }
}

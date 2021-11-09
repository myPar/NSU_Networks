package ThreadParts;

import Structures.HostTable;
import Structures.Message;
import Structures.MySocket;
import Structures.View;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Core {
    // app port
    private int port;
    // local host address
    private InetAddress selfAddress;
    // host id
    private long id;
    // delta time of packets sending (in milliseconds)
    private final int sendDeltaTime = 10;
    // socket
    private MySocket socket;
    // host table
    private HostTable table;
    // GUI
    private View view;
// core components:
    // sender
    private Sender sender;
    // receiver
    private Receiver receiver;
    // time checker
    private TimeChecker timeChecker;
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
                    e.printStackTrace();
                    System.err.println("Fatal: InterruptedException");
                    System.exit(1);
                }
            }
        }
    }
// nested class Receiver; receives messages in the separate thread from multi-cast group
    private class Receiver extends Thread {
        @Override
        public void run() {
            assert socket != null;
            while (true) {
                // try to receive message
                Message message = null;
                try {
                    message = socket.getMessage();
                } catch (MySocket.SocketException e) {
                    // non fatal message receiving exception
                    e.printException();
                    continue;
                }
                // get host key (name + id)
                String key = message.getHostKey();

                // check multi-cast message from itself
                if (key.equals(selfAddress.getHostAddress() + id)) {
                    continue;
                }
                // check message from the existing host
                if (table.contains(key)) {
                    table.updateHost(key);
                }
                else {
                    // add new host in table
                    table.addHost(message.getHostAddress(), message.getLaunchId());
                }
            }
        }
    }
// nested class TimeChecker; removes disconnected hosts
    private class TimeChecker extends Thread {
        // delta time of host checking (in milliseconds)
        private final int checkDeltaTime = 100;
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
                    System.out.println("Fatal: InterruptedException");
                    System.exit(1);
                }
            }
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
            e.printStackTrace();
            System.err.println("Fatal: can't get local host address");
            System.exit(1);
        }
        // generate id
        id = System.currentTimeMillis();
        //init GUI
        view = new View("Self ip address: " + selfAddress.getHostAddress() + " self id: " + id + "\n" + "Other copies: \n");
        // create empty host table and configure it
        table = new HostTable(view);
        table.config(150);
        // init Core components:
        sender = new Sender();
        receiver = new Receiver();
        timeChecker = new TimeChecker();
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
            e.printStackTrace();
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
        view.viewGUI();
        // start components execution:
        sender.start();
        receiver.start();
        timeChecker.start();
    }
}

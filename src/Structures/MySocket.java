package Structures;

import Structures.Message;

import java.io.IOException;
import java.net.*;

public class MySocket {
    // TODO 1. write exception types and handling for each of it : DONE
    //      2. write datagram sending and receiving (send, read methods in MySocket class)
    //      3. write app core (main algorithm of work)
    //      4. write time counter checker
    // Socket exception class
    public static class SocketException extends Exception {
        // exception type enum
        public enum Type{
            SOCKET_CREATE("SOCKET_CREATE"), JOIN("JOIN"), GET_LOCAL_ADDRESS("GET_LOCAL_ADDRESS");
            private final String message;
            // enum constructor
            Type(String m) {message = m;}
            // message getter method
            String getMessage() {return  message;}
        }
        // exception message
        private String message;
        // exception type
        private Type type;
        // constructor
        public SocketException(Type t, String m) {
            message = m;
            type = t;
        }
        // print exception message method
        public void printException() {
            System.err.println("Exception of type '" + type.getMessage() + "': " + message);
            printStackTrace();
        }
    }
    // network type enum
    public enum NetworkType{IPV6, IPV4};

    // multi-cast socket fo message forwarding
    private MulticastSocket socket;
    // address of multi-cast group
    private InetAddress groupAddress;
    // port number
    private int port;
    // message size
    private int messageSize;
    // own hostAddress
    private InetAddress selfAddress;

    MySocket(InetAddress gAddress, int p, NetworkType type) throws SocketException {
        groupAddress = gAddress;
        port = p;
        // try to create socket
        try {
            // create multi-cast socket
            socket = new MulticastSocket(port);
        } catch (IOException e) {
            throw new SocketException(SocketException.Type.SOCKET_CREATE, "can't create socket on port - " + port);
        }
        // try join to multi-cast group
        try {
            socket.joinGroup(groupAddress);
        }
        catch (IOException e) {
            throw new SocketException(SocketException.Type.JOIN, "can't join to group with address - " + gAddress);
        }
        // check network type
        if (type == NetworkType.IPV4) {
            try {
                selfAddress = Inet4Address.getLocalHost();
            } catch (UnknownHostException e) {
                throw new SocketException(SocketException.Type.GET_LOCAL_ADDRESS, "can't get local host address; type - IPv4");
            }
        }
        else if (type == NetworkType.IPV6){
            try {
                selfAddress = Inet6Address.getLocalHost();
            } catch (UnknownHostException e) {
                throw new SocketException(SocketException.Type.GET_LOCAL_ADDRESS, "can't get local host address; type - IPv6");
            }
        } else {assert false;}
    }
    // send message method
    void sendMessage(Message message) {

    }
    // get message from socket
    Message getMessage() {
        return null;
    }
}

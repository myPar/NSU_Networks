package Structures;

import Structures.Message;

import java.io.IOException;
import java.net.*;

public class MySocket {
// Socket exception class
    public static class SocketException extends Exception {
        // exception type enum
        public enum Type{
            SOCKET_CREATE("SOCKET_CREATE"), JOIN("JOIN"), GET_LOCAL_ADDRESS("GET_LOCAL_ADDRESS"),
            SEND("SEND"), RECEIVE("RECEIVE");
            // string representation field
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
// fields
    // multi-cast socket fo message forwarding
    private MulticastSocket socket;
    // address of multi-cast group
    private InetAddress groupAddress;
    // port number
    private int port;
    // message size
    private final int maxMessageSize = 1000;
// constructor
    public MySocket(InetAddress gAddress, int p) throws SocketException {
        groupAddress = gAddress;
        port = p;
        // try to create socket
        try {
            // create multi-cast socket
            socket = new MulticastSocket(port);
        } catch (IOException e) {
            throw new SocketException(SocketException.Type.SOCKET_CREATE, "can't create socket on port - " + port);
        }
        // try to join to multi-cast group
        try {
            socket.joinGroup(groupAddress);
        }
        catch (IOException e) {
            throw new SocketException(SocketException.Type.JOIN, "can't join to group with address - " + gAddress);
        }
    }
    // send message method
    public void sendMessage(Message message) throws SocketException {
        // get message data
        String messageData = message.getHostAddress() + " " + message.getLaunchId() + " ";
        // create datagram
        DatagramPacket packet = new DatagramPacket(messageData.getBytes(), messageData.length(), groupAddress, port);
        // send datagram
        try {
            socket.send(packet);
        } catch (IOException e) {
            throw new SocketException(SocketException.Type.SEND, "Can't send datagram packet. address: " +
                                      groupAddress.getHostAddress() + ", port: " + port + ", length: " + messageData.length());
        }
    }
    // get message from socket
    public Message getMessage() throws SocketException {
        byte[] buf = new byte[maxMessageSize];
        DatagramPacket receivePacket = new DatagramPacket(buf, buf.length);
        String[] data;
        // receive datagram
        try {
            socket.receive(receivePacket);
            data = (new String(receivePacket.getData())).split(" ");
        } catch (IOException e) {
            throw new SocketException(SocketException.Type.RECEIVE, "can't receive datagram packet: " +
                    groupAddress.getHostAddress() + ", port: " + port + ", buff size - " + maxMessageSize);
        }
        // check IP in received message
        String strAddress = data[0];
        try {
            InetAddress address = InetAddress.getByName(strAddress);
        } catch (UnknownHostException e) {
            throw new SocketException(SocketException.Type.RECEIVE, "Invalid IP address: " + data[0] + " in received message");
        }
        // check id in received message
        long id;
        try {
            id = Long.parseLong(data[1]);
        } catch (NumberFormatException e) {
            throw new SocketException(SocketException.Type.RECEIVE, "Invalid id: " + data[1] + " in received message");
        }
        // return received message
        return new Message(strAddress, id);
    }
}

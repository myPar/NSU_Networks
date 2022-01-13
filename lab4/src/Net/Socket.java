package Net;

import Protocol.CountProvider;
import Protocol.Data;
import Protocol.Message;

import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.List;

// UDP - socket implementation
public class Socket implements ChannelProvider {
    private final int MAX_BUFF_SIZE = 8192;
    private byte[] receiveBuffer = new byte[MAX_BUFF_SIZE];

    public static class SocketException extends Exception {
        public final String message;

        SocketException(String m) {
            message = m;
        }
    }
    private InetAddress groupAddress;
    private int groupPort;

    // sockets 1 - receives multicast messages; 2 - receiving/sending other messages
    private MulticastSocket groupMessageReceiver;
    private DatagramSocket socket;

    public Socket(String groupAddress, int groupPort) throws IOException {
        this.groupAddress = InetAddress.getByName(groupAddress);
        this.groupPort = groupPort;

        groupMessageReceiver = new MulticastSocket(groupPort);
        groupMessageReceiver.joinGroup(this.groupAddress);
        socket = new DatagramSocket();  // bind to available port on the local host machine
    }
    @Override
    public synchronized void sendMulticastInitMessage(List<Data.GamePlayer> players, Data.GameConfig config, boolean can_join) throws Exception {
        long msg_seq = CountProvider.provideCount();
        Message.InitMessage message = new Message.InitMessage(players, config, can_join);
        message.set_common(msg_seq, -1, -1);    // sender and receiver id doesn't matter at the start

        // serialize message
        byte[] data = serialize(message);

        // create multicast datagram packet and send it throw the socket
        DatagramPacket packet = new DatagramPacket(data, data.length, groupAddress, groupPort);
        socket.send(packet);
    }

    @Override
    public synchronized void sendPingMessage(InetAddress ip, int port, long sender_id, long receiver_id) throws Exception {
        Message.PingMessage message = new Message.PingMessage();

        try { send(message, ip, port, sender_id, receiver_id);}
        catch (IOException e) {
            throw new SocketException("I/O exception while sending ping message");
        }
    }

    @Override
    public synchronized void sendJoinMessage(InetAddress ip, int port, String name, boolean onlyView, Data.PlayerType type) throws Exception {
        Message.JoinMessage message = new Message.JoinMessage(type, onlyView, name);

        try { send(message, ip, port, -1, -1);}
        catch (IOException e) {
            throw new SocketException("I/O exception while sending join message");
        }
    }

    @Override
    public synchronized void sendAcceptMessage(InetAddress ip, int port, long sender_id, long receiver_id) throws Exception {
        Message.AcceptMessage message = new Message.AcceptMessage();

        try {send(message, ip, port, sender_id, receiver_id);}
        catch (IOException e) {
            throw new SocketException("I/O exception while sending AcceptMessage");
        }
    }

    @Override
    public AddressedMessage getMulticastMessage() throws Exception {
        // receive multicast group messages throw multicast socket
        DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
        // use multicast group socket group
        groupMessageReceiver.receive(receivePacket);

        return getMessage(receivePacket);
    }

    @Override
    public AddressedMessage getUnicastMessage() throws Exception {
        DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
        // use default socket
        socket.receive(receivePacket);

        return getMessage(receivePacket);
    }

    // converts message to byte array
    private byte[] serialize(Message message) throws IOException {
        byte[] result;

        try(ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream(); ObjectOutputStream objOut = new ObjectOutputStream(byteBuffer)) {
            objOut.writeObject(message);
            objOut.flush();

            result = byteBuffer.toByteArray();
        }
        return result;
    }
    // get Message object fom byte array
    private Message deserialize(byte[] arr) throws IOException, ClassNotFoundException {
        Message result;

        try(ByteArrayInputStream byteBuffer = new ByteArrayInputStream(arr); ObjectInputStream objIn = new ObjectInputStream(byteBuffer)) {
            result = (Message) objIn.readObject();
        }
        return result;
    }
    // send Message method
    private void send(Message message, InetAddress ip, int port, long sender_id, long receiver_id) throws IOException {
        long msg_seq = CountProvider.provideCount();
        message.set_common(msg_seq, sender_id, receiver_id);

        // serialize message
        byte[] data = serialize(message);
        // create datagram packet and send it
        DatagramPacket packet = new DatagramPacket(data, data.length, ip, port);
        socket.send(packet);
    }
    // get message object from datagram method
    private AddressedMessage getMessage(DatagramPacket receivedDatagram) throws SocketException {
        int len = receivedDatagram.getLength();
        byte[] data = Arrays.copyOf(receivedDatagram.getData(), len);

        Message message;
        try {
            message = deserialize(data);
        }
        catch (IOException io) {
            throw new SocketException("I/O exception while object deserialization");
        }
        catch (ClassNotFoundException cnf) {
            throw new SocketException("class no found exception while object deserialization");
        }
        InetAddress senderAddress = receivedDatagram.getAddress();
        int senderPort = receivedDatagram.getPort();

        return new AddressedMessage(message, senderAddress, senderPort);
    }
}

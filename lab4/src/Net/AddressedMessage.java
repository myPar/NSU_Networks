package Net;

import Protocol.Message;

import java.net.InetAddress;

public class AddressedMessage {
    private Message message;
    private final InetAddress senderAddress;
    private final int senderPort;

    public Message getMessage() {
        return message;
    }

    public InetAddress getSenderAddress() {
        return senderAddress;
    }

    public int getSenderPort() {
        return senderPort;
    }

    public AddressedMessage(Message message, InetAddress senderAddress, int senderPort) {
        this.message = message;
        this.senderAddress = senderAddress;
        this.senderPort = senderPort;
    }
}

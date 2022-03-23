package SOCKS;

public class ConnectionMessage {
    public final byte commandCode;
    public final byte AddressType;
    public final String addressValue;   // IP address or domain name
    public final int portNumber;

    public ConnectionMessage(byte code, byte type, String address, int port) {
        this.AddressType = type;
        this.addressValue = address;
        this.commandCode = code;
        this.portNumber = port;
    }
}

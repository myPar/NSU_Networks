package Attachments;

// contains information about connection request: port, address type and address value
public class ConnectionRequestData {
    public final byte portByte1;
    public final byte portByte2;
    public final byte[] address;
    public final byte addressType;

    public ConnectionRequestData(byte b1, byte b2, byte[] address, byte addressType) {
        portByte1 = b1;
        portByte2 = b2;
        this.address = address;
        this.addressType = addressType;
    }
}

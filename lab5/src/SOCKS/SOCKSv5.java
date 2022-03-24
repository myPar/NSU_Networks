package SOCKS;

import Attachments.BaseAttachment.KeyState;
import Attachments.CompleteAttachment;
import Core.Constants;
import Exceptions.SocksException;
import Exceptions.SocksException.Classes;
import Exceptions.SocksException.Types;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class SOCKSv5 {
    // Socks version, here - 5'th
    private static final byte SOCKS_VERSION = 0x05;
    // no authentication required
    private static final byte AUTH_METHOD = 0x00;
    // establish TCP connection command available
    private static final byte ESTABLISH_TCP = 0x01;
    // Reserved byte
    private static final byte RESERVED = 0x00;
    // unsupported authentication method
    private static final byte UNSUPPORTED_AUTH_METHOD = (byte) 0xff;

    // Inet Addresses types:
    public static final byte IPV4 = 0x01;
    public static final byte IPV6 = 0x04;
    public static final byte DN = 0x03;

    //server response codes:
    private static final byte REQUEST_PROVIDED = 0x00;
    private static final byte SOCKS_SERVER_EXCEPTION = 0x01;
    private static final byte HOST_UNAVAILABLE = 0x04;

    // other constants:
    private static final int IPV4_SIZE = 4;
    private static final int IPV6_SIZE = 16;
    private static final int AUTH_METHODS_COUNT = 1;
    private static final int INIT_MSG_HEADER_SIZE = 2;
    private static final int CONNECT_MSG_BASE_SIZE = 6;

    // methods for getting server response data:
    public static byte[] getSuccessInitResponse() {
        return new byte[]{SOCKS_VERSION, AUTH_METHOD};
    }
    public static byte[] getFailedInitResponse() {
        return new byte[]{SOCKS_VERSION, UNSUPPORTED_AUTH_METHOD};
    }
    public static byte[] getConnectionResponse(CompleteAttachment keyAttachment) {

    }
    // throws SOCKS exception if 'init message' is invalid
    public static void parseInitRequest(byte[] data) throws SocksException {
        if (data.length < INIT_MSG_HEADER_SIZE) {
            throw new SocksException(Classes.INIT_RQST, "invalid request message size", Types.FORMAT);
        }
        if (data[0] != SOCKS_VERSION) {
            throw new SocksException(Classes.INIT_RQST, "invalid socks version", Types.VERSION);
        }
        if (data[1] <= 0) {
            throw new SocksException(Classes.INIT_RQST, "invalid authentication methods count", Types.FORMAT);
        }
        int count = data[1];    // authentication methods count

        // check data size:
        if (data.length != INIT_MSG_HEADER_SIZE + count) {
            throw new SocksException(Classes.INIT_RQST, "invalid request message size", Types.FORMAT);
        }
        // check 'no auth required' method supporting:
        boolean found = false;
        for (int i = INIT_MSG_HEADER_SIZE; i < count; i++) {
            if (data[i] == AUTH_METHOD) {
                found = true;
                break;
            }
        }
        if (!found) {
            throw new SocksException(Classes.INIT_RQST, "suggested auth methods are not supported", Types.AUTH);
        }
    }
    // parses byte array of connection request message, throws SOCKS exception if message is invalid
    public static ConnectionMessage parseConnectRequest(byte[] msg) throws SocksException {
        // check header:
        if (msg.length < CONNECT_MSG_BASE_SIZE) {
            throw new SocksException(Classes.CONNECT_RQST, "invalid connect message format", Types.FORMAT);
        }
        if (msg[0] != SOCKS_VERSION) {
            throw new SocksException(Classes.CONNECT_RQST, "invalid socks version", Types.VERSION);
        }
        if (msg[1] != ESTABLISH_TCP) {
            throw new SocksException(Classes.CONNECT_RQST, "unsupported operation", Types.VERSION);
        }
        if (msg[2] != RESERVED) {
            throw new SocksException(Classes.CONNECT_RQST, "invalid reserved byte", Types.FORMAT);
        }
        byte addressType = msg[3];
        byte commandCode = msg[1];
        String address = null;

        // getting address value:
        switch (addressType) {
            case IPV4:
                checkMsgSize(msg, IPV4_SIZE + CONNECT_MSG_BASE_SIZE);
                address = getIP(Arrays.copyOfRange(msg, 4, 4 + IPV4_SIZE));
                break;
            case IPV6:
                checkMsgSize(msg, IPV6_SIZE + CONNECT_MSG_BASE_SIZE);
                address = getIP(Arrays.copyOfRange(msg, 4, 4 + IPV6_SIZE));
                break;
            case DN:
                int nameLength = msg[4];
                checkMsgSize(msg, CONNECT_MSG_BASE_SIZE + nameLength + 1);
                address = new String(Arrays.copyOfRange(msg, 5, 5 + nameLength));
            default:
                assert false;
        }
        // getting port:
        int lastIdx = msg.length - 1;
        int port = getPort(msg[lastIdx - 1], msg[lastIdx]);

        return new ConnectionMessage(commandCode, addressType, address, port);
    }
    // decode ip address from byte array
    private static String getIP(byte[] data) throws SocksException {
        assert data.length == 4 || data.length == 6;
        String result;
        try {
            InetAddress address = InetAddress.getByAddress(data);
            result = address.getHostAddress();
        }
        catch (UnknownHostException e) {
            throw new SocksException(Classes.CONNECT_RQST, "invalid ip-address of destination host", Types.HOST_IP);
        }
        return result;
    }
    // return int value of port
    private static int getPort(byte b1, byte b2) {
        return (b1 << Constants.BYTE_SIZE) | b2;
    }
    // return two bytes of the port
    public static byte[] getPortBytes(int port) {
        int size = Constants.INT_SIZE;
        byte[] data = ByteBuffer.allocate(size).putInt(port).array();

        return new byte[]{data[size - 2], data[size - 1]};
    }
    // check msg size and trow exception if it is invalid
    private static void checkMsgSize(byte[] msg, int size) throws SocksException {
        if (msg.length != size) {
            throw new SocksException(Classes.CONNECT_RQST, "invalid message size", Types.FORMAT);
        }
    }
}

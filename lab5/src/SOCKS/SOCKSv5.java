package SOCKS;

import Exceptions.SocksException;
import Exceptions.SocksException.Classes;
import Exceptions.SocksException.Types;

import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.UnknownHostException;
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
    private static final byte IPV4 = 0x01;
    private static final byte IPV6 = 0x04;
    private static final byte DN = 0x03;

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
    // throws SOCKS exception if 'connection request' message is invalid
    public final ConnectionMessage parseConnectRequest(byte[] msg) throws SocksException {
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
        int addressType = msg[3];
        String address;
        byte[] addressData = null;
        try {
            switch (addressType) {
                case IPV4:
                    addressData = Arrays.copyOfRange(msg, 4, 4 + IPV4_SIZE);
                    break;
                case IPV6:
                    addressData = Arrays.copyOfRange(msg, 4, 4 + IPV6_SIZE);
                    break;
                case DN:
                    // TODO Domain Name resolving
                default:
                    assert false;
            }
            address = InetAddress.getByAddress(addressData).getHostAddress();
        }
        catch (UnknownHostException e) {
            throw new SocksException(Classes.CONNECT_RQST, "invalid ip-address of destination host", Types.HOST_IP);
        }
        return new
    }
    //
    private String getIP(byte[] data) throws SocksException {
        assert data.length == 4 || data.length == 6;
        String result;
        try {
            InetAddress address = InetAddress.getByAddress(data);
            result = address.getHostAddress();
        }
        catch (UnknownHostException e) {
            throw new SocksException(Classes.CONNECT_RQST, "invalid IP address", Types.HOST_IP);
        }
        return result;
    }
}

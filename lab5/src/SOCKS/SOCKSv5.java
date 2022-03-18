package SOCKS;

import Exceptions.SocksException;
import Exceptions.SocksException.Classes;
import Exceptions.SocksException.Types;

import java.lang.reflect.Type;

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

    // methods for getting server response data:
    public static byte[] getSuccessInitResponse() {
        return new byte[]{SOCKS_VERSION, AUTH_METHOD};
    }
    public static byte[] getFailedInitResponse() {
        return new byte[]{SOCKS_VERSION, UNSUPPORTED_AUTH_METHOD};
    }
    // throws SOCKS exception if 'init message' is invalid
    public static void parseInitRequest(byte[] data) throws SocksException {
        if (data.length < 2) {
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
        if (data.length != 2 + count) {
            throw new SocksException(Classes.INIT_RQST, "invalid request message size", Types.FORMAT);
        }
        // heck 'no auth required' method supporting:
        boolean found = false;
        for (int i = 2; i < count; i++) {
            if (data[i] == AUTH_METHOD) {
                found = true;
                break;
            }
        }
        if (!found) {
            throw new SocksException(Classes.INIT_RQST, "suggested auth methods are not supported", Types.AUTH);
        }
    }
}

package SOCKS;

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
    public static byte[] getInitResponse() {
        return new byte[]{SOCKS_VERSION, AUTH_METHOD};
    }
}

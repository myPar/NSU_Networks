class Constants {
    static final int MAX_CLIENT_COUNT = Integer.MAX_VALUE;
    static final int MAX_BUFFER_SIZE = 8192;
    static final int HELLO_MESSAGE_SIZE = 3;
    static final int CONNECT_MESSAGE_SIZE = 6;
    static final int CONNECT_MESSAGE_IPv4 = 10;
    static final int CONNECT_MESSAGE_DN = 8;
    // exception codes
    static final byte SOCKS_ERROR = (byte) 0x01;
    static final byte CONNECTION_UNAVAILABLE = (byte) 0x03;
    static final byte HOST_UNAVAILABLE = (byte) 0x04;
    static final byte PROTOCOL_ERROR = (byte) 0x07;  // and also - a protocol error
    static final byte ADDRESS_TYPE_UNSUPPORTED = (byte) 0x08;
    static final byte INIT_REQUEST_ERROR = (byte) 0xff;
    // other codes
    static final byte REQUEST_PROVIDED = (byte) 0x00;
    static final byte SOCKS_VERSION = (byte) 0x05;
    static final byte IP = (byte) 0x01;
    static final byte DN = (byte) 0x03;
    static final byte RESERVED = (byte) 0x00;
}

package Exceptions;

public class SocksException extends BaseException {
    public static class Classes {
        public static final String INIT_RQST = "SOCKS: Init request";
        public static final String CONNECT_RQST = "SOCKS: Connection request";
    }
    public static class Types {
        public static final String FORMAT = "message format";
        public static final String VERSION = "socks version";
        public static final String AUTH = "authentication methods";
        public static final String HOST_IP = "ip address";
    }
    public SocksException(String cls, String dscr, String type) {
        super(cls, dscr, type);
    }
}

package Exceptions;

public class HandlerException extends BaseException {
    // exception classes (handler types):
    public static class Classes {
        public static final String ACCEPT = "Accept handler";
        public static final String INIT_RQST = "Init request handler";
        public static final String INIT_RESPONSE = "Init response handler";
        public static final String CONNECTION_RQST = "Connection request handler";
        public static final String CONNECTION_RESPONSE = "Connection response handler";
        public static final String DNS_RESPONSE = "DNS response handler";
        public static final String PROXYING = "Proxying handler";
        public static final String FINISH_CONNECTION = "Finish connection handler";
        public static final String CLOSE_CHANNEL = "Close channel handler";
    }
    // exception types - types of exceptions, which handler can produce
    public static class Types {
        public static final String IO = "I/O";
        public static final String NO_DATA = "no available data";
        public static final String UH = "Unknown Host";
        public static final String FINISH = "finishing the server's work";
    }

    public HandlerException(String cls, String dscr, String type) {
        super(cls, dscr, type);
    }
}

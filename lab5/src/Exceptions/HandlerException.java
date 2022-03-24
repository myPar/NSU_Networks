package Exceptions;

public class HandlerException extends BaseException {
    // exception classes (handler types):
    public static class Classes {
        public static final String ACCEPT = "Accept handler";
        public static final String INIT_RQST = "Init request handler";
        public static final String CONNECTION_RQST = "Connection request handler";
        public static final String FINISH_CONNECTION = "Finish connection handler";
    }
    // exception types - types of java exceptions, which handler can produce
    public static class Types {
        public static final String IO = "I/O";
        public static final String UH = "Unknown Host";
    }

    public HandlerException(String cls, String dscr, String type) {
        super(cls, dscr, type);
    }
}

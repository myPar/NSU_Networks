package Exceptions;

public class HandlerException extends BaseException {
    // exception classes (handler types):
    public static class Classes {
        public static final String ACCEPT = "Accept handler";
        public static final String INIT_RQST = "Init request handler";
    }
    // exception types - types of java exceptions, which handler can produce
    public static class Types {
        public static final String IO = "I/O";
    }

    public HandlerException(String cls, String dscr, String type) {
        super(cls, dscr, type);
    }
}

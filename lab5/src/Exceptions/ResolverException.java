package Exceptions;

public class ResolverException extends BaseException {
    public static class Classes {
        public static final String REQUEST = "dns request";
        public static final String RESPONSE = "dns response";
    }
    public static class Types {
        public static final String FORMAT = "format";
        public static final String UH = "unknown host";
        public static final String IO = "I/O";
        public static final String CONNECT = "connection";
    }
    public ResolverException(String cls, String dscr, String type) {
        super(cls, dscr, type);
    }
}

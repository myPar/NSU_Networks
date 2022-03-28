package Exceptions;

public class ServerException extends BaseException {
    public static class Types {
        public static final String INIT = "Init server";
        public static final String SELECT = "select channels";
    }
    public ServerException(String type, String dscr) {
        super("server", dscr, type);
    }
}

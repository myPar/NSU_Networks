package Exceptions;

public class ServerException extends BaseException {
    public enum ServerExceptionType {
        SERVER_START("SERVER_START");
        private String value;
        ServerExceptionType(String val) {value = val;}

        public final String getValue() {return value;}
    }
    public ServerException(ServerExceptionType type, String dscr) {
        this.description = dscr;
        this.exceptionType = type.getValue();
        this.exceptionClass = "server";
    }
}

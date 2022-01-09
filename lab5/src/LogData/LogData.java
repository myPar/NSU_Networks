package LogData;

public class LogData {
    public static class Type {
        public static final String START = "START SERVER";
        public static final String ACCEPT = "NEW CLIENT ACCEPTED";
        public static final String CONNECT = "CONNECT TO REMOTE POINT";
        public static final String READ = "READ DATA";
        public static final String WRITE = "WRITE DATA";
        public static final String CLOSE = "CLOSE CHANNEL";
    }
    public static class Status {
        public static final String SUCCESS = "SUCCESS";
        public static final String IN_PROCESS = "IN PROCESS";
        public static final String FAILED = "FAILED";
    }
    public static String getMessage(String type, String status, String m) {
        return type + " - " + status + ". " + m + "\n";
    }
}

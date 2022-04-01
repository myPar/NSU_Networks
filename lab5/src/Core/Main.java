package Core;

import Logger.GlobalLogger;

public class Main {
    private static final int ARG_NUMBER = 1;
    public static void main(String[] args) {
        if (args.length != ARG_NUMBER) {
            System.err.println("invalid args count - " + args.length);
            System.exit(1);
        }
        int port = 0;
        try {
            port = Integer.parseInt(args[0]);
        }
        catch (NumberFormatException e) {
            System.err.println("invalid port number - " + args[0]);
            System.exit(1);
        }
        Server.start(port, GlobalLogger.Mode.ENABLE);
    }

}

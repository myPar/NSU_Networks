public class Main {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("invalid args count: " + args.length + ", should be 1 - port");
            return;
        }
        int port;
        try {
            port = Integer.parseInt(args[0]);
            System.out.println("port - " + port);
        }
        catch(NumberFormatException e) {
            System.err.println("invalid port: " + args[0]);
            return;
        }
        Server proxy = new Server();
        proxy.start(port);
    }
}

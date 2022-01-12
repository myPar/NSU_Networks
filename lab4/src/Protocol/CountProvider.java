package Protocol;

public class CountProvider {
    private static final int sleepTime = 1; // 1ms
    private static final int errorCode = 1;

    // sleeping on 1ms guarantied that count will be unique
    public static synchronized long provideCount() {
        long result = System.currentTimeMillis();
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            System.err.println("Fatal: interrupted exception while unique count generation");
            System.exit(errorCode);
        }
        return result;
    }
}

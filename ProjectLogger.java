public class ProjectLogger {
    public static synchronized void log(long timeMs, String threadName, String message) {
        System.out.printf("[%04d ms] [%s] %s%n", timeMs, threadName, message);
    }
}
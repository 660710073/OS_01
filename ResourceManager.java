import java.util.concurrent.Semaphore;

public class ResourceManager {
    private final Semaphore printerSemaphore;
    private final Semaphore databaseSemaphore;

    public ResourceManager(int printerPermits, int databasePermits) {
        this.printerSemaphore = new Semaphore(printerPermits, true);
        this.databaseSemaphore = new Semaphore(databasePermits, true);
    }

    public void acquireResource(String resourceType) throws InterruptedException {
        if (resourceType == null) return;
        
        String type = resourceType.toUpperCase();
        if (type.equals("PRINTER")) {
            printerSemaphore.acquire();
        } else if (type.equals("DATABASE")) {
            databaseSemaphore.acquire();
        }
    }

    public void releaseResource(String resourceType) {
        if (resourceType == null) return;

        String type = resourceType.toUpperCase();
        if (type.equals("PRINTER")) {
            printerSemaphore.release();
        } else if (type.equals("DATABASE")) {
            databaseSemaphore.release();
        }
    }
}
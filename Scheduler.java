import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

public class Scheduler implements Runnable {
    private final BlockingQueue<Job> arrivalQueue;
    private final BlockingQueue<Job> readyQueue;
    private final String policy;
    private volatile boolean running = true;

    public Scheduler(BlockingQueue<Job> arrivalQueue, String policy, int initialCapacity) {
        this.arrivalQueue = arrivalQueue;
        this.policy = policy.toLowerCase();

            if (this.policy.equals("priority")) {
        this.readyQueue = new PriorityBlockingQueue<>(initialCapacity, (j1, j2) -> {
            int prioCompare = Integer.compare(j1.getPriority(), j2.getPriority());
            if (prioCompare != 0) {
                return prioCompare;
            }
            
            int arrivalCompare = Long.compare(j1.getArrivalMs(), j2.getArrivalMs());
            if (arrivalCompare != 0) {
                return arrivalCompare;
            }

            return j1.getId().compareTo(j2.getId());
        });
    } else {
        this.readyQueue = new java.util.concurrent.LinkedBlockingQueue<>();
    }
}

    @Override
    public void run() {
        try {
            while (running) {
                Job job = arrivalQueue.take();
                
                if (job == Job.POISON_PILL) {
                    readyQueue.put(job);
                    break;
                }
                
                readyQueue.put(job);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public BlockingQueue<Job> getReadyQueue() {
        return readyQueue;
    }

    public void stopScheduler() {
        this.running = false;
    }
}
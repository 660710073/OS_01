import java.util.concurrent.BlockingQueue;

public class Monitor implements Runnable {
    private final Statistics statistics;
    private final BlockingQueue<Job> readyQueue;
    private volatile boolean running = true;

    public Monitor(Statistics statistics, BlockingQueue<Job> readyQueue) {
        this.statistics = statistics;
        this.readyQueue = readyQueue;
    }

    @Override
    public void run() {
        try {
            long simulationStart = System.currentTimeMillis();
            while (running) {
                // รอประมาณ 1,000 มิลลิวินาทีตามข้อกำหนด[cite: 3, 7]
                Thread.sleep(1000);

                // ดึงข้อมูลภาพรวมแบบ Thread-safe
                int readyCount = readyQueue.size();
                int runningCount = statistics.getRunningCount();
                int completedCount = statistics.getCompletedCount();

                long currentTime = System.currentTimeMillis() - simulationStart;
                
                // พิมพ์รายงานสถานะระบบเป็น Snapshot ตามข้อกำหนด[cite: 3, 7]
                System.out.printf("[%04d ms] [Monitor] Ready: %d | Running: %d | Completed: %d%n",
                        currentTime, readyCount, runningCount, completedCount);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void stopMonitor() {
        this.running = false;
    }
}
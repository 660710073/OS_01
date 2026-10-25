import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Main {
    public static void main(String[] args) {
        // 1. ตรวจสอบพารามิเตอร์ Command-line ให้ครบ 5 ตัว[cite: 7]
        if (args.length != 5) {
            printUsage();
            System.exit(1);
        }

        String workloadFile = args[0];
        String policy = args[1].toLowerCase();
        int workersCount = 0;
        int printerPermits = 0;
        int databasePermits = 0;

        try {
            workersCount = Integer.parseInt(args[2]);
            printerPermits = Integer.parseInt(args[3]);
            databasePermits = Integer.parseInt(args[4]);
        } catch (NumberFormatException e) {
            System.err.println("Error: พารามิเตอร์ที่เป็นตัวเลข (workers, printerPermits, databasePermits) ต้องเป็นจำนวนเต็มเท่านั้น");
            printUsage();
            System.exit(1);
        }

        if (!policy.equals("fcfs") && !policy.equals("priority")) {
            System.err.println("Error: Policy ต้องเป็น 'fcfs' หรือ 'priority' เท่านั้น");
            printUsage();
            System.exit(1);
        }

        try {
            // 2. โหลด Workload เพื่อเช็คจำนวน Job ทั้งหมดในไฟล์
            List<Job> jobs = WorkloadLoader.loadJobs(workloadFile);
            int totalJobs = jobs.size();

            // 3. สร้างคิวและออบเจ็กต์กลางของระบบ[cite: 2, 3, 5]
            BlockingQueue<Job> arrivalQueue = new LinkedBlockingQueue<>();
            ResourceManager resourceManager = new ResourceManager(printerPermits, databasePermits);
            Statistics statistics = new Statistics();

            long simulationStart = System.currentTimeMillis();
            System.out.println("=== เริ่มต้นจำลองระบบ (Simulation Started) ===");

            // 4. สร้างและเริ่ม JobGenerator Thread[cite: 2, 3]
            JobGenerator generator = new JobGenerator(arrivalQueue, workloadFile);
            Thread generatorThread = new Thread(generator, "JobGenerator");
            generatorThread.start();

            // 5. สร้างและเริ่ม Scheduler Thread[cite: 2, 3]
            Scheduler scheduler = new Scheduler(arrivalQueue, policy, totalJobs);
            Thread schedulerThread = new Thread(scheduler, "Scheduler");
            schedulerThread.start();

            // 6. สร้างและเริ่ม Worker Threads หลายตัวตามจำนวนที่ระบุ[cite: 2]
            Thread[] workerThreads = new Thread[workersCount];
            Worker[] workers = new Worker[workersCount];
            for (int i = 0; i < workersCount; i++) {
                workers[i] = new Worker("Worker-" + (i + 1), scheduler.getReadyQueue(), resourceManager, statistics);
                workerThreads[i] = new Thread(workers[i], "Worker-" + (i + 1));
                workerThreads[i].start();
            }

            // 7. สร้างและเริ่ม Monitor Thread เพื่อรายงานสถานะทุกๆ 1 วินาที[cite: 3, 7]
            Monitor monitor = new Monitor(statistics, scheduler.getReadyQueue());
            Thread monitorThread = new Thread(monitor, "Monitor");
            monitorThread.start();

            // 8. รอให้ JobGenerator ส่งงานเข้าคิวครบทั้งหมด
            generatorThread.join();

            // 9. กลไก Graceful Shutdown: รอจนกว่าจำนวนงานที่ทำเสร็จเท่ากับจำนวนงานทั้งหมดในระบบ
            while (statistics.getCompletedCount() < totalJobs) {
                Thread.sleep(100);
            }

            // 10. เมื่อมั่นใจว่างานเสร็จครบทุกตัวแล้ว ให้ส่งสัญญาณหยุด Thread ทุกตัวอย่างปลอดภัย
            scheduler.stopScheduler();
            schedulerThread.interrupt();

            for (int i = 0; i < workersCount; i++) {
                workers[i].stopWorker();
                workerThreads[i].interrupt();
            }

            monitor.stopMonitor();
            monitorThread.interrupt();

            // รอให้ทุก Thread ปิดตัวลงอย่างสมบูรณ์ (Join) โดยไม่มี Thread ค้างใน JVM[cite: 6]
            schedulerThread.join();
            for (int i = 0; i < workersCount; i++) {
                workerThreads[i].join();
            }
            monitorThread.join();

            long totalSimulationTime = System.currentTimeMillis() - simulationStart;
            System.out.println("=== สิ้นสุดการจำลองระบบ (Simulation Ended) ===");

            // 11. พิมพ์ตารางสรุปผลและค่า Metrics ต่างๆ[cite: 2]
            statistics.printSummary(totalSimulationTime);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void printUsage() {
        System.err.println("Usage: java Main <workload.csv> <fcfs|priority> <workers> <printerPermits> <databasePermits>");
        System.err.println("Example: java Main jobs_standard.csv priority 3 1 2");
    }
}
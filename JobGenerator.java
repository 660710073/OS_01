import java.util.List;
import java.util.concurrent.BlockingQueue;

public class JobGenerator implements Runnable {
    private final BlockingQueue<Job> arrivalQueue;
    private final String workloadFile;

    // Constructor รับค่าคิวขาเข้าและชื่อไฟล์ workload
    public JobGenerator(BlockingQueue<Job> arrivalQueue, String workloadFile) {
        this.arrivalQueue = arrivalQueue;
        this.workloadFile = workloadFile;
    }

    @Override
    public void run() {
        try {
            // 1. โหลดข้อมูล Job จากไฟล์ CSV ผ่าน WorkloadLoader
            List<Job> jobs = WorkloadLoader.loadJobs(workloadFile);
            
            // บันทึกเวลาเริ่มต้นจำลองระบบ
            long simulationStart = System.currentTimeMillis();

            // 2. วนลูปปล่อย Job ทีละตัวตามเวลา arrivalMs
            for (Job job : jobs) {
                long currentSimulationTime = System.currentTimeMillis() - simulationStart;
                long targetArrivalMs = job.getArrivalMs();

                // ถ้ายังไม่ถึงเวลามาถึง ให้ Thread นอนรอ (Sleep)
                if (targetArrivalMs > currentSimulationTime) {
                    Thread.sleep(targetArrivalMs - currentSimulationTime);
                }

                // 3. นำ Job ใส่ลงใน arrivalQueue (BlockingQueue)
                arrivalQueue.put(job);

                // 4. บันทึก Log เหตุการณ์ ARRIVED
                long logTime = System.currentTimeMillis() - simulationStart;
                ProjectLogger.log(logTime, "JobGenerator", job.getId() + " ARRIVED priority = " + job.getPriority());
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("JobGenerator ถูกขัดจังหวะการทำงาน (Interrupted)");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
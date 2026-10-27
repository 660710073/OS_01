import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
/**
 * Thread ที่ดึงงานจาก Ready Queue ไปทำจนเสร็จ
 *
 * ===== ไฟล์นี้เป็นโครงเปล่า นักศึกษาต้องเขียนเอง =====
 *
 * ลำดับการทำงานของ Job หนึ่งชิ้น บังคับตามหัวข้อ 6 ของโจทย์:
 *   1. รับงานจาก Ready Queue แล้วบันทึกเวลาเริ่ม
 *   2. จำลองงานหลักด้วย Thread.sleep(job.workMs)
 *   3. ถ้า job.resource != NONE ให้บันทึกเวลาเริ่มรอ แล้ว acquire
 *   4. จำลองการถือครองด้วย Thread.sleep(job.resourceMs)
 *   5. release แล้วบันทึกเวลาจบ
 *
 * ห้ามสลับขั้นที่ 2 กับ 3 เพราะจะทำให้ผลของทุกกลุ่มเทียบกันไม่ได้
 *
 * จุดที่มักพลาด:
 *   - ถ้า exception หรือ interrupt เกิดขึ้นหลัง acquire แต่ก่อน release
 *     permit จะค้างถาวรและระบบจะแขวน ต้องออกแบบให้คืนได้เสมอ
 *   - Worker ต้องหยุดเองได้เมื่อไม่มีงานเหลือแล้ว ไม่ใช่วนรอตลอดไป
 */
public class Worker extends Thread {

    // TODO: เก็บ ReadyQueue, ResourceManager, Statistics และ logger
    private final ReadyQueue readyQueue;
    private final ResourceManager resources;
    private final Statistics statistics;
    private final ProjectLogger logger;

    private final AtomicInteger runningCount;
    private final CountDownLatch completionLatch;

    public Worker(String name, ReadyQueue readyQueue, ResourceManager resources,
                  Statistics statistics, ProjectLogger logger, AtomicInteger runningCount, CountDownLatch completionLatch) {
        super(name);
        // TODO
        this.readyQueue = readyQueue;
        this.resources = resources;
        this.statistics = statistics;
        this.logger = logger;
        this.runningCount = runningCount;
        this.completionLatch = completionLatch;
    }

    @Override
    public void run() {
        // TODO: วนรับงานและเรียก processJob จนกว่าจะได้รับสัญญาณให้หยุด
        try {
            while (true) {
                // รอรับงานจากคิว (ถ้าคิวว่าง จะหลับรอเองโดยไม่กิน CPU)
                Job job = readyQueue.take();
                
                // นำงานไปทำตาม 5 ขั้นตอน
                try {
                    runningCount.incrementAndGet(); // เพิ่มยอดงานที่กำลังทำ (สำหรับให้ Monitor ดึงไปแสดง)
                    processJob(job);
                } finally {
                    runningCount.decrementAndGet(); // ลดลอดยอดงานที่กำลังทำ
                    completionLatch.countDown(); // ส่งสัญญาณบอก Main ว่างานเสร็จไปอีก 1 ชิ้นแล้ว
                }
            }
        } catch (InterruptedException e) {
            // ถ้าระบบสั่งปิดให้ออกจากลูป
            Thread.currentThread().interrupt();
          }
    }

    /** ทำงานหนึ่งชิ้นให้จบตามลำดับ 5 ขั้นด้านบน */
    private void processJob(Job job) throws InterruptedException {
        // TODO
        // ขั้นที่ 1. รับงานจาก Ready Queue แล้วบันทึกเวลาเริ่ม
        job.startWorkMs = logger.now();
        logger.jobStarted(job);

        // ขั้นที่ 2. จำลองงานหลักด้วย Thread.sleep(job.workMs)
        Thread.sleep(job.workMs);
        logger.workFinished(job);

        // ขั้นที่ 3. ถ้า job.resource != NONE ให้บันทึกเวลาเริ่มรอ แล้ว acquire
        if (job.resource != ResourceType.NONE) {
            job.startResourceWaitMs = logger.now();
            logger.resourceWaitStarted(job);
            
            // ขอคิวใช้ทรัพยากร (อาจจะต้องรอถ้ายุ่งอยู่)
            resources.acquire(job.resource);
            
            // เมื่อได้คิวแล้ว คำนวณว่ารอนานแค่ไหน
            long waitedMs = logger.now() - job.startResourceWaitMs;
            job.totalResourceWaitMs = waitedMs;
            logger.resourceAcquired(job, waitedMs);

            // ขั้นที่ 4. จำลองการถือครองด้วย Thread.sleep(job.resourceMs)
            Thread.sleep(job.resourceMs);

            // ขั้นที่ 5. release แล้วบันทึกเวลาจบ
            resources.release(job.resource);
            logger.resourceReleased(job);
        }

        // ขั้นที่ 5 (ต่อ) บันทึกเวลาจบงาน
        job.finishMs = logger.now();
        logger.jobCompleted(job);
        
        // ส่งข้อมูลให้ Statistics บันทึกว่ามีงานเสร็จเพิ่ม 1 ชิ้น
        if (statistics != null) {
            statistics.recordCompletion(job);
        }
    }
}

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * จุดเริ่มต้นของโปรแกรม
 *
 * ===== ไฟล์นี้เป็นโครงเปล่า นักศึกษาต้องเขียนเอง =====
 *
 * ส่วนที่เขียนไว้ให้แล้วคือการรับค่า การโหลด workload และการแสดง error
 * ซึ่งไม่ใช่สิ่งที่โครงงานนี้วัด ส่วนที่เหลือเป็น TODO ทั้งหมด
 *
 * วิธีรัน:
 *   java Main jobs_standard.csv priority 3 1 2
 */
public class Main {

    public static void main(String[] args) {
        // ---------- 1. รับค่าจาก command line ----------
        Config config;
        try {
            config = Config.parse(args);
        } catch (IllegalArgumentException e) {
            System.err.println("ผิดพลาด: " + e.getMessage());
            System.err.println();
            System.err.println(Config.USAGE);
            System.exit(1);
            return;
        }

        // ---------- 2. เริ่มจับเวลาและโหลด workload ----------
        ProjectLogger logger = new ProjectLogger();
        List<Job> jobs;
        try {
            jobs = WorkloadLoader.load(config.workloadPath);
        } catch (WorkloadFormatException e) {
            System.err.println("ไฟล์ workload ผิดรูปแบบ — " + e.getMessage());
            System.exit(1);
            return;
        } catch (java.io.IOException e) {
            System.err.println("เปิดไฟล์ \"" + config.workloadPath + "\" ไม่ได้");
            System.err.println("ตรวจว่าไฟล์มีอยู่จริงและ path ถูกต้อง (สั่ง java จากโฟลเดอร์ใด)");
            System.exit(1);
            return;
        }
        logger.systemStart(config);
        logger.systemEvent("โหลดงานได้ " + jobs.size() + " ชิ้น");

        // ---------- 3. สร้างส่วนประกอบของระบบ ----------
        // TODO: สร้าง ResourceManager จากจำนวน permit ใน config
        // TODO: สร้าง ReadyQueue ตามนโยบายใน config
        // TODO: สร้าง Statistics
        ResourceManager resources = new ResourceManager(config.printerPermits, config.databasePermits);
        ReadyQueue readyQueue = new ReadyQueue(config.policy);
        Statistics statistics = new Statistics();

        // เครื่องมือพิเศษ: ตัวแปรแชร์ข้อมูลระหว่าง Thread
        AtomicInteger runningCount = new AtomicInteger(0);
        CountDownLatch completionLatch = new CountDownLatch(jobs.size()); // ใช้เทคนิค CountDownLatch เพื่อนับงาน
        BlockingQueue<Job> arrivalQueue = new LinkedBlockingQueue<>(); // ท่อส่งงานสำหรับ JobGenerator -> Scheduler

        // ---------- 4. สร้างและเริ่ม Thread ----------
        // TODO: สร้าง Worker จำนวน config.workers ตัว แล้ว start
        // TODO: สร้างและ start Scheduler
        // TODO: สร้างและ start Monitor
        // TODO: สร้างและ start JobGenerator
        //
        List<Worker> workers = new ArrayList<>();
        for (int i = 1; i <= config.workers; i++) {
            Worker w = new Worker("worker-" + i, readyQueue, resources, statistics, logger, runningCount, completionLatch);
            workers.add(w);
            w.start();
        }

        // สร้างไว้ที่พื้นที่หลัก
        Scheduler scheduler = new Scheduler(arrivalQueue, readyQueue);
        scheduler.start();

        Monitor monitor = new Monitor(readyQueue, resources, statistics, logger, runningCount);
        monitor.start();

        JobGenerator generator = new JobGenerator(jobs, arrivalQueue, logger);
        generator.start();
        // ลำดับการ start มีผลหรือไม่ ให้คิดและอธิบายได้ใน Demo

        // ---------- 5. รอจนงานเสร็จครบ ----------
        // TODO: รอจนกว่างานทั้ง jobs.size() ชิ้นจะเสร็จ
        //
        try {
            // ระบบจะหยุดรอที่บรรทัดนี้จนกว่า Latch จะถูกนับถอยหลังถึง 0 (งานชิ้นสุดท้ายเคลียร์เสร็จ) โดยไม่กิน CPU
            completionLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        // *** นี่คือจุดที่ยากที่สุดของโครงงานนี้ ***
        // Worker ที่กำลังรออยู่ในคิวไม่มีทางรู้ได้เองว่าจะไม่มีงานเข้ามาอีกแล้ว
        // กลุ่มต้องออกแบบวิธีบอก โดยห้ามใช้การเดาเวลา เช่น sleep(10000)
        //
        // เทคนิคที่ไปหาอ่านต่อได้ (เลือกใช้อันใดอันหนึ่งหรือผสมกันก็ได้):
        //   - poison pill
        //   - CountDownLatch
        //   - ตัวนับงานค้างที่ป้องกันด้วย lock
        //
        // อาการผิดที่ต้องไม่เกิด:
        //   1. main จบแล้วแต่ JVM ไม่ปิด เพราะยังมี Thread ค้างอยู่
        //   2. Worker หยุดก่อนที่งานชิ้นสุดท้ายจะทำเสร็จ
        //   3. permit ค้างเพราะถูก interrupt ระหว่างถือ resource

        // ---------- 6. สั่งหยุดทุก Thread ----------
        // TODO: หยุด Worker ทุกตัว, Scheduler, Monitor และ JobGenerator
        // TODO: join ทุก Thread เพื่อยืนยันว่าหยุดจริงก่อนไปขั้นถัดไป
        generator.interrupt();
        scheduler.interrupt(); 
        monitor.interrupt();
        for (Worker w : workers) {
            w.interrupt(); // ปลุก Worker ที่หลับรอคิวงานว่างให้ยุติการทำงาน
        }

        try {
            generator.join();
            scheduler.join();
            monitor.join();
            for (Worker w : workers) {
                w.join();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // ---------- 7. สรุปผล ----------
        // TODO: หา makespan = เวลาที่งานชิ้นสุดท้ายเสร็จ (ใช้ logger.now())
        // TODO: เรียก statistics.printSummary(jobs, makespanMs)
        // TODO: logger.systemStop(completed, jobs.size())
        long makespanMs = logger.now();
        statistics.printSummary(jobs, makespanMs);
        logger.systemStop(statistics.completedCount(), jobs.size());
    }
}

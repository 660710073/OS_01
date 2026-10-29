import java.util.concurrent.atomic.AtomicInteger;
/**
 * Thread ที่รายงานสถานะระบบเป็นระยะ
 *
 * ===== ไฟล์นี้เป็นโครงเปล่า นักศึกษาต้องเขียนเอง =====
 *
 * ข้อกำหนดจากโจทย์ (หัวข้อ 10):
 *   - รายงานประมาณทุก 1,000 ms ไม่ต้องแม่นตรงทุกครั้ง
 *   - อย่างน้อยต้องมี ready, running, completed และสถานะการใช้ resource
 *   - ข้อมูลที่อ่านต้องเป็น snapshot ที่ปลอดภัย
 *     โดยเฉพาะตัวนับ running ซึ่ง Worker หลายตัวเพิ่ม/ลดพร้อมกัน
 *   - ห้ามอ่าน collection หรือตัวนับที่กำลังถูกแก้ไขโดยไม่มีการป้องกัน
 *
 * ให้พิมพ์ผ่าน logger.monitor(ready, running, completed, resources.status())
 * เพื่อให้รูปแบบตรงกับกลุ่มอื่น
 *
 * ข้อควรคิด: ตัวนับ running ควรอยู่ที่ไหน ใครเป็นคนเพิ่มและลด
 * และจะอ่านพร้อมกับ ready กับ completed ให้เป็นภาพเดียวกันได้อย่างไร
 */
public class Monitor extends Thread {

    // TODO: เก็บสิ่งที่ต้องอ่านสถานะ และ logger//
    private final ReadyQueue readyQueue;
    private final ResourceManager resources;
    private final Statistics statistics;
    private final ProjectLogger logger;

    // เพิ่มตัวแปรสำหรับรับค่าจำนวนงานที่กำลังทำงานอยู่ ตามคำแนะนำของอาจารย์
    private final AtomicInteger runningCount; 
    
    private volatile boolean isRunning = true;
    // หมายเหตุ: constructor ด้านล่างยังไม่มีทางเข้าถึงตัวนับ running
    // เพราะยังไม่มีการตัดสินว่าตัวนับนั้นควรอยู่ที่ไหน ให้เพิ่ม parameter
    // เข้าไปเองเมื่อออกแบบเสร็จ

    public Monitor(ReadyQueue readyQueue, ResourceManager resources,
                   Statistics statistics, ProjectLogger logger, AtomicInteger runningCount) {
        super("monitor");
        // TODO
        this.readyQueue = readyQueue;
        this.resources = resources;
        this.statistics = statistics;
        this.logger = logger;
        this.runningCount = runningCount;
    }

    @Override
    public void run() {
        // TODO: วนรายงานสถานะทุก ~1000 ms จนกว่าจะได้รับสัญญาณให้หยุด
        try {
            while (isRunning) {
                // พักรอ 1 วินาที
                Thread.sleep(1000);
                
                // ถ่ายภาพ snapshot สถานะปัจจุบันอย่างปลอดภัย
                int ready = readyQueue.size();
                int running = runningCount.get();
                int completed = statistics.completedCount();
                String resStatus = resources.status();
                
                // สั่งพิมพ์รายงาน
                logger.monitor(ready, running, completed, resStatus);
            }
        } catch (InterruptedException e) {
            // ถ้าระบบสั่งปิด (interrupt) ให้ออกจากลูปการทำงาน
            Thread.currentThread().interrupt();
        }
    }
    public  void shutdown() {
        isRunning = false;
        this.interrupt();
    }
}

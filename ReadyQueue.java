import java.util.Comparator;
import java.util.concurrent.PriorityBlockingQueue;
/**
 * คิวงานที่พร้อมถูกหยิบไปทำ
 *
 * ===== ไฟล์นี้เป็นโครงเปล่า นักศึกษาต้องเขียนเอง =====
 *
 * สิ่งที่คลาสนี้ต้องทำได้:
 *   - เก็บงานที่รอ Worker อยู่
 *   - หยิบงานถัดไปตามนโยบายที่เลือก (FCFS หรือ Priority)
 *   - ถูกเรียกจากหลาย Thread พร้อมกันได้อย่างปลอดภัย
 *
 * ข้อกำหนดจากโจทย์ที่เกี่ยวกับคลาสนี้:
 *   - หัวข้อ 4: priority = 1 สูงสุด เมื่อเท่ากันต้องมีกติกาตัดสินลำดับ (tie-break)
 *     ที่ตัดสินจากข้อมูลของ Job ไม่ขึ้นกับว่า Thread ใดเข้าถึงคิวก่อน
 *   - หัวข้อ 7: ห้ามวนลูปเช็กแบบกิน CPU (busy waiting) — Worker ที่ไม่มีงานทำ
 *     ต้องถูกพักไว้ ไม่ใช่วนถามซ้ำ ๆ
 *
 * จะออกแบบเป็นคลาสเดียวที่รับนโยบายเข้ามา หรือแยกเป็นสองคลาส
 * หรือใช้โครงสร้างข้อมูลสำเร็จรูปของ Java ก็ได้ ขอให้อธิบายเหตุผลได้ใน Demo
 */
public class ReadyQueue {
    private final PriorityBlockingQueue<Job> queue;
    // TODO: เก็บนโยบาย (Config.Policy) และโครงสร้างข้อมูลที่ใช้เก็บงาน

    public ReadyQueue(Config.Policy policy) {
        // TODO
        Comparator<Job> comparator;
        if (policy == Config.Policy.FCFS) {
            // FCFS: เรียงตามลำดับที่อ่านเจอในไฟล์ (sequence)
            comparator = Comparator.comparingInt(j -> j.sequence);
        } else {
            // PRIORITY: เรียงตาม priority ก่อน ถ้าเท่ากันให้ตัดสินด้วย sequence
            comparator = (j1, j2) -> {
                if (j1.priority != j2.priority) {
                    return Integer.compare(j1.priority, j2.priority);
                }
                return Integer.compare(j1.sequence, j2.sequence);
            };
        }
        // สร้าง PriorityBlockingQueue โดยใช้ comparator ที่กำหนด
        this.queue = new PriorityBlockingQueue<>(11, comparator);
    }

    /** ใส่งานเข้าคิว เรียกโดย Scheduler Thread */
    public void add(Job job) {
        // TODO
        queue.put(job);
    }

    /**
     * หยิบงานถัดไปตามนโยบาย เรียกโดย Worker Thread
     *
     * ถ้ายังไม่มีงาน ต้องรอโดยไม่กิน CPU
     * ต้องคิดด้วยว่าจะบอก Worker อย่างไรเมื่อไม่มีงานเหลือแล้วและควรหยุดทำงาน
     */
    public Job take() throws InterruptedException {
        // TODO
        return queue.take();
    }

    /** จำนวนงานที่รออยู่ตอนนี้ ใช้โดย Monitor — ต้องอ่านได้อย่างปลอดภัย */
    public int size() {
        // TODO
        return queue.size();
    }
}

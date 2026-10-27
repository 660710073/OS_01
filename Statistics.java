import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * รวบรวมและคำนวณค่าที่ใช้วัดผลของการรันหนึ่งครั้ง
 *
 * ===== ไฟล์นี้เป็นโครงเปล่า นักศึกษาต้องเขียนเอง =====
 *
 * ข้อกำหนดจากโจทย์ที่เกี่ยวกับคลาสนี้ (หัวข้อ 8):
 *   - Waiting Time, Turnaround Time, Throughput, Resource Wait Time
 *   - ต้องถูกอัปเดตจากหลาย Worker พร้อมกันได้อย่างปลอดภัย
 *   - ผลต้องสอดคล้องกับสมการตรวจสอบ:
 *       Turnaround = Waiting + workMs + Resource Wait + resourceMs
 *     ใช้สมการนี้ตรวจงานทีละชิ้นได้ว่าค่าไหนคำนวณผิด
 *
 * ข้อควรระวัง: ค่าเฉลี่ยของ Resource Wait ให้คิดเฉพาะงานที่ใช้ resource
 * ส่วนงานที่ resource = NONE ให้ถือว่า Resource Wait เป็น 0
 */
public class Statistics {

    // TODO: เก็บข้อมูลของงานที่เสร็จแล้ว หรือเก็บผลรวมไว้คำนวณทีหลัง
    private final AtomicInteger completedJobs = new AtomicInteger(0);
    /** บันทึกว่างานชิ้นหนึ่งเสร็จแล้ว เรียกโดย Worker หลายตัวพร้อมกันได้ */
    public void recordCompletion(Job job) {
        // TODO
        completedJobs.incrementAndGet();
    }

    /** จำนวนงานที่เสร็จแล้ว ใช้โดย Monitor และใช้ตรวจว่างานครบหรือยัง */
    public int completedCount() {
        // TODO
        return completedJobs.get();
    }

    /**
     * พิมพ์ตารางสรุปผลตอนจบโปรแกรม
     * อย่างน้อยต้องมี avg Waiting Time, avg Turnaround Time,
     * Throughput และ avg Resource Wait Time
     *
     * ตามหัวข้อ 14 ให้รายงานเวลาเป็นจำนวนเต็มหน่วย ms
     * และ Throughput อย่างน้อย 2 ตำแหน่งทศนิยม
     */
    public void printSummary(List<Job> allJobs, long makespanMs) {
        // TODO
        long totalWaitMs = 0;
        long totalTurnaroundMs = 0;
        long totalResourceWaitMs = 0;
        int jobsWithResource = 0;

        for (Job job : allJobs) {
            // คำนวณ Waiting Time (เวลารอในคิว) = เวลาที่เริ่มทำจริง - เวลาที่มาถึงจริง
            long waitMs = job.startWorkMs - job.actualArrivalMs;
            
            // คำนวณ Turnaround Time (เวลาตอบสนองรวม) = เวลาที่ทำเสร็จ - เวลาที่มาถึงจริง
            long turnaroundMs = job.finishMs - job.actualArrivalMs;
            
            totalWaitMs += waitMs;
            totalTurnaroundMs += turnaroundMs;

            // ค่าเฉลี่ยของ Resource Wait ให้คิดเฉพาะงานที่ใช้ resource ตามข้อกำหนด
            if (job.resource != ResourceType.NONE) {
                totalResourceWaitMs += job.totalResourceWaitMs;
                jobsWithResource++;
            }
        }

        int totalJobs = allJobs.size();
        
        // โจทย์บังคับรายงานเวลาเป็นจำนวนเต็มหน่วย ms
        long avgWait = totalJobs > 0 ? totalWaitMs / totalJobs : 0;
        long avgTurnaround = totalJobs > 0 ? totalTurnaroundMs / totalJobs : 0;
        long avgResourceWait = jobsWithResource > 0 ? totalResourceWaitMs / jobsWithResource : 0;
        
        // Throughput อย่างน้อย 2 ตำแหน่งทศนิยม (งานต่อวินาที)
        double makespanSec = makespanMs / 1000.0;
        double throughput = makespanSec > 0 ? totalJobs / makespanSec : 0.0;

        System.out.println("\n=== SIMULATION SUMMARY ===");
        System.out.println("Total Completed Jobs : " + totalJobs);
        System.out.println("Total Simulation Time: " + makespanMs + " ms");
        System.out.println("Average Waiting Time : " + avgWait + " ms");
        System.out.println("Average Turnaround   : " + avgTurnaround + " ms");
        System.out.println("Average Resource Wait: " + avgResourceWait + " ms");
        System.out.printf("Throughput           : %.2f jobs/sec\n", throughput);
        System.out.println("==========================");
    }
}

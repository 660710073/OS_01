public class Job {
    private String id;
    private long arrivalMs;
    private int priority;
    private long workMs;
    private String resource;
    private long resourceMs;

    // ตัวแปรสำหรับเก็บเวลาและค่า Metrics ต่างๆ
    private long startTime;
    private long completionTime;
    private long waitingTime;
    private long turnaroundTime;
    private long resourceWaitTime;

    // สัญญาณ Poison Pill สำหรับใช้หยุด Thread ตอนปิดระบบ (Graceful Shutdown)
    public static final Job POISON_PILL = new Job("POISON", -1, -1, -1, "NONE", 0);

    // Constructor สำหรับสร้างออบเจ็กต์ Job
    public Job(String id, long arrivalMs, int priority, long workMs, String resource, long resourceMs) {
        this.id = id;
        this.arrivalMs = arrivalMs;
        this.priority = priority;
        this.workMs = workMs;
        this.resource = resource;
        this.resourceMs = resourceMs;
    }

    // --- Getters ---
    public String getId() {
        return id;
    }

    public long getArrivalMs() {
        return arrivalMs;
    }

    public int getPriority() {
        return priority;
    }

    public long getWorkMs() {
        return workMs;
    }

    public String getResource() {
        return resource;
    }

    public long getResourceMs() {
        return resourceMs;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getCompletionTime() {
        return completionTime;
    }

    public long getWaitingTime() {
        return waitingTime;
    }

    public long getTurnaroundTime() {
        return turnaroundTime;
    }

    public long getResourceWaitTime() {
        return resourceWaitTime;
    }

    // --- Setters ---
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public void setCompletionTime(long completionTime) {
        this.completionTime = completionTime;
    }

    public void setWaitingTime(long waitingTime) {
        this.waitingTime = waitingTime;
    }

    public void setTurnaroundTime(long turnaroundTime) {
        this.turnaroundTime = turnaroundTime;
    }

    public void setResourceWaitTime(long resourceWaitTime) {
        this.resourceWaitTime = resourceWaitTime;
    }
}
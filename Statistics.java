import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Statistics {
    private final List<Job> completedJobs = Collections.synchronizedList(new ArrayList<>());
    
    private final AtomicInteger runningJobsCount = new AtomicInteger(0);
    private final AtomicInteger completedJobsCount = new AtomicInteger(0);

    public void incrementRunning() {
        runningJobsCount.incrementAndGet();
    }

    public void decrementRunning() {
        runningJobsCount.decrementAndGet();
    }

    public void addCompletedJob(Job job) {
        completedJobs.add(job);
        completedJobsCount.incrementAndGet();
    }

    public int getRunningCount() {
        return runningJobsCount.get();
    }

    public int getCompletedCount() {
        return completedJobsCount.get();
    }

    public List<Job> getCompletedJobs() {
        return new ArrayList<>(completedJobs);
    }

    public void printSummary(long totalSimulationTimeMs) {
        synchronized (completedJobs) {
            if (completedJobs.isEmpty()) {
                System.out.println("No completed jobs to summarize.");
                return;
            }

            long totalWaitingTime = 0;
            long totalTurnaroundTime = 0;
            long totalResourceWaitTime = 0;
            int resourceWaitCount = 0;

            for (Job job : completedJobs) {
                totalWaitingTime += job.getWaitingTime();
                totalTurnaroundTime += job.getTurnaroundTime();
                
                String res = job.getResource();
                if (res != null && !res.equalsIgnoreCase("NONE")) {
                    totalResourceWaitTime += job.getResourceWaitTime();
                    resourceWaitCount++;
                }
            }

            int n = completedJobs.size();
            double avgWaitingTime = (double) totalWaitingTime / n;
            double avgTurnaroundTime = (double) totalTurnaroundTime / n;
            double avgResourceWaitTime = resourceWaitCount > 0 ? (double) totalResourceWaitTime / resourceWaitCount : 0.0;
            double throughput = (double) n / (totalSimulationTimeMs / 1000.0);

            System.out.println("\nSIMULATION SUMMARY");
            System.out.println("Total Completed Jobs : " + n);
            System.out.println("Total Simulation Time: " + totalSimulationTimeMs + " ms");
            System.out.printf("Average Waiting Time : %.2f ms%n", avgWaitingTime);
            System.out.printf("Average Turnaround   : %.2f ms%n", avgTurnaroundTime);
            System.out.printf("Average Resource Wait: %.2f ms%n", avgResourceWaitTime);
            System.out.printf("Throughput           : %.2f jobs/sec%n", throughput);
            System.out.println("\n");
        }
    }
}
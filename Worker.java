import java.util.concurrent.BlockingQueue;

public class Worker implements Runnable {
    private final String workerName;
    private final BlockingQueue<Job> readyQueue;
    private final ResourceManager resourceManager;
    private final Statistics statistics;
    private volatile boolean running = true;

    public Worker(String workerName, BlockingQueue<Job> readyQueue, ResourceManager resourceManager, Statistics statistics) {
        this.workerName = workerName;
        this.readyQueue = readyQueue;
        this.resourceManager = resourceManager;
        this.statistics = statistics;
    }

    @Override
    public void run() {
        try {
            long simulationStart = System.currentTimeMillis();

            while (running) {
                Job job = readyQueue.take();

                if (job == Job.POISON_PILL) {
                    readyQueue.put(job);
                    break;
                }

                long startTime = System.currentTimeMillis() - simulationStart;
                job.setStartTime(startTime);
                ProjectLogger.log(startTime, workerName, job.getId() + " START");

                Thread.sleep(job.getWorkMs());

                long resourceWaitTime = 0;
                String resourceType = job.getResource();

                if (resourceType != null && !resourceType.equalsIgnoreCase("NONE")) {
                    long waitStart = System.currentTimeMillis();
                    ProjectLogger.log(waitStart - simulationStart, workerName, job.getId() + " WAIT " + resourceType);

                    resourceManager.acquireResource(resourceType);

                    long acquiredTime = System.currentTimeMillis();

                    resourceWaitTime = acquiredTime - waitStart;
                    job.setResourceWaitTime(resourceWaitTime);

                    ProjectLogger.log(acquiredTime - simulationStart, workerName, job.getId() + " ACQUIRE " + resourceType);

                    try {
                        Thread.sleep(job.getResourceMs());
                    } finally {
                        resourceManager.releaseResource(resourceType);
                        ProjectLogger.log(System.currentTimeMillis() - simulationStart, workerName, job.getId() + " RELEASE " + resourceType);
                    }
                } else {
                    job.setResourceWaitTime(0);
                }

                long completionTime = System.currentTimeMillis() - simulationStart;
                job.setCompletionTime(completionTime);

                long waitingTime = job.getStartTime() - job.getArrivalMs();
                long turnaroundTime = job.getCompletionTime() - job.getArrivalMs();
                job.setWaitingTime(waitingTime);
                job.setTurnaroundTime(turnaroundTime);

                statistics.addCompletedJob(job);

                ProjectLogger.log(completionTime, workerName, job.getId() + " COMPLETE");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void stopWorker() {
        this.running = false;
    }
}
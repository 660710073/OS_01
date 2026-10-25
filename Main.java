public class Main {
    public static void main(String[] args) {
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

        System.out.println("System Configuration");
        System.out.println("Workload File  : " + workloadFile);
        System.out.println("Scheduling     : " + policy.toUpperCase());
        System.out.println("Worker Threads : " + workersCount);
        System.out.println("Printer Permits: " + printerPermits);
        System.out.println("DB Permits     : " + databasePermits);
        System.out.println("\n");
    }
    
    private static void printUsage() {
        System.err.println("Usage: java Main <workload.csv> <fcfs|priority> <workers> <printerPermits> <databasePermits>");
        System.err.println("Example: java Main jobs_standard.csv priority 3 1 2");
    }
}

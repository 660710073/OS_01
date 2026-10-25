import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WorkloadLoader {
    public static List<Job> loadJobs(String filePath) {
        List<Job> jobs = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isFirstLine = true;
            
            // ตรงนี้ต้องเป็นตัว L ใหญ่ (readLine)
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (isFirstLine && (line.toLowerCase().contains("id") || line.toLowerCase().contains("arrival"))) {
                    isFirstLine = false;
                    continue;
                }
                isFirstLine = false;
                String[] parts = line.split(",");
                if (parts.length >= 6) {
                    jobs.add(new Job(parts[0].trim(), Long.parseLong(parts[1].trim()), Integer.parseInt(parts[2].trim()), 
                             Long.parseLong(parts[3].trim()), parts[4].trim(), Long.parseLong(parts[5].trim())));
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading workload file: " + e.getMessage());
        }
        return jobs;
    }
}
package aitu.edu;

import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        String targetDir = "data/";
        List<String> inputFiles = FileUtils.getJsonFiles(targetDir);
        for (String fileName : inputFiles) {
            System.out.println("Processing file: " + fileName);
            String inputPath = targetDir + fileName;
            String baseName = fileName.substring("input_".length());
            String outputPath = targetDir + "report_" + baseName;
            TasksReportGenerator.generateReport(inputPath, outputPath);
            System.out.println("Report generated: " + outputPath);
        }
    }
}
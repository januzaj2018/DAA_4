package aitu.edu;

import java.util.List;

/**
 * Main entry point for the application that processes input JSON files and generates reports.
 */
public class Main {
    /**
     * Main method that processes all input JSON files in the data directory and generates corresponding report files.
     *
     * @param args command line arguments (not used)
     * @throws Exception if there is an issue during processing
     */
    public static void main(String[] args) throws Exception {
        // Define the target directory containing input files
        String targetDir = "data/";
        // Retrieve the list of JSON files in the target directory
        List<String> inputFiles = FileUtils.getJsonFiles(targetDir);
        // Process each input file
        for (String fileName : inputFiles) {
            System.out.println("Processing file: " + fileName);
            String inputPath = targetDir + fileName;
            String baseName = fileName.substring("input_".length());
            String outputPath = targetDir + "report_" + baseName;
            // Generate report for the current input file
            TasksReportGenerator.generateReport(inputPath, outputPath);
            System.out.println("Report generated: " + outputPath);
        }
    }
}
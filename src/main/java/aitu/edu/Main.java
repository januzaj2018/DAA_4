package aitu.edu;

import java.util.*;

public class Main {
    public static void main(String[] args) throws Exception {
        String input = args.length > 0 ? args[0] : "data/tasks.json";
        String output = args.length > 1 ? args[1] : "data/reports.json";

        // Generate reports for graphs in the input file and write JSON to the output path
        TasksReportGenerator.generateReport(input, output);

        System.out.println("Report generated: " + output);
    }
}
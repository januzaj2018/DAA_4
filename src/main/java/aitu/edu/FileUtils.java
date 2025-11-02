package aitu.edu;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility class for file operations, such as retrieving lists of JSON files from a directory.
 */
public class FileUtils {
    /**
     * Retrieves a list of JSON files starting with "input_" from the specified directory.
     *
     * @param directory the path to the directory to search
     * @return a list of JSON file names matching the pattern
     * @throws IOException if there is an issue accessing the directory
     */
    public static List<String> getJsonFiles(String directory) throws IOException {
        Path dirPath = Paths.get(directory);
        // Check if the provided path is a directory
        if (!Files.isDirectory(dirPath)) {
            System.err.println("Provided path is not a directory: " + directory);
            return List.of();
        }
        // List and filter files in the directory
        try (Stream<Path> paths = Files.list(dirPath)) {
            return paths.filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .filter(name -> name.startsWith("input_") && name.endsWith(".json"))
                    .collect(Collectors.toList());
        }
    }
}

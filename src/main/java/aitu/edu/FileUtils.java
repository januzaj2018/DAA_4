package aitu.edu;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileUtils {
    public static List<String> getJsonFiles(String directory) throws IOException {
        Path dirPath = Paths.get(directory);
        if (!Files.isDirectory(dirPath)) {
            System.err.println("Provided path is not a directory: " + directory);
            return List.of();
        }
        try (Stream<Path> paths = Files.list(dirPath)) {
            return paths.filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .filter(name -> name.startsWith("input_") && name.endsWith(".json"))
                    .collect(Collectors.toList());
        }
    }
}

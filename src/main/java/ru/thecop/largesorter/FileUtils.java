package ru.thecop.largesorter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

final class FileUtils {
    private FileUtils() {
    }

    static File createOrReplaceFile(Path filePath) throws IOException {
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        }
        Files.createFile(filePath);
        return new File(filePath.toUri());
    }
}

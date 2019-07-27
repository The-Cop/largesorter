package ru.thecop.largesorter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;

import static ru.thecop.largesorter.FileUtils.createOrReplaceFile;

class LargeFileGenerator {
    private static final Random random = new Random();
    static final String GENERATED_FILE_PATH = "d:\\Temp\\genfile.txt";

    private static final ArrayList<Character> ALLOWED_CHARS;

    static {
        ALLOWED_CHARS = new ArrayList<>();
        for (int i = 'a'; i <= 'z'; i++) {
            ALLOWED_CHARS.add((char) i);
        }
        for (int i = 'A'; i <= 'Z'; i++) {
            ALLOWED_CHARS.add((char) i);
        }
        for (int i = '0'; i <= '9'; i++) {
            ALLOWED_CHARS.add((char) i);
        }
    }

    public static void main(String[] args) throws IOException {
//        generate(10000, 10000);//approx 45-50 mb
        generate(20000, 20000);//approx 150-200 mb
    }

    private static void generate(int lineCount, int maxLineLength) throws IOException {
        Path path = Paths.get(GENERATED_FILE_PATH);
        createOrReplaceFile(path);
        File file = new File(path.toUri());
        try (FileWriter fileWriter = new FileWriter(file)) {
            for (int i = 0; i < lineCount; i++) {
                if (i != 0) {
                    fileWriter.write(System.lineSeparator());
                }
                fileWriter.write(generateLine(maxLineLength));
            }
        }
    }

    private static String generateLine(int maxLineLength) {
        StringBuilder buffer = new StringBuilder();
        int length = 1 + random.nextInt(maxLineLength);
        for (int i = 0; i < length; i++) {
            int charIndex = random.nextInt(ALLOWED_CHARS.size());
            buffer.append(ALLOWED_CHARS.get(charIndex));
        }
        return buffer.toString();
    }
}

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
    private static String GENERATED_FILE_PATH = null;

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
        if (args.length < 3) {
            System.err.println("Specify lines count, max line length and file path. " +
                    "Example: 100 100 \"c:\\temp\\generated.txt\"");
            return;
        }
        int lineCount = Integer.valueOf(args[0]);
        int maxLineLength = Integer.valueOf(args[1]);
        GENERATED_FILE_PATH = args[2];
        System.out.println("Generating " + lineCount
                + " lines of max " + maxLineLength +
                " length to file " + GENERATED_FILE_PATH);
        generate(lineCount, maxLineLength);//10000+10000 = approx 45-50 mb
        System.out.println("Done.");
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

package ru.thecop.largesorter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static ru.thecop.largesorter.FileUtils.createOrReplaceFile;

class LargeFileSorter {
    private static final long CHUNK_LIMIT_BYTES = 1024 * 1024 * 20; //20Mb
    private static final String CHUNK_FILE_NAME = "chunk";
    private static final String RESULT_FILE_NAME = "result.txt";


    public static void main(String[] args) throws IOException {
        Path path = Paths.get(LargeFileGenerator.GENERATED_FILE_PATH);
        System.out.println("File size = " + Files.size(path) / 1024 + " kb");
        sortLargeFile(path);
    }

    private static void sortLargeFile(Path path) throws IOException {
        int chunksCount = splitFileToSortedChunks(path);
        Path resultPath = mergeChunks(path.getParent(), chunksCount);
        validateFileSorted(resultPath);//optional validation
    }

    private static int splitFileToSortedChunks(Path path) {
        StringBuffer buffer = new StringBuffer();
        List<String> chunkLines = new ArrayList<>();
        int chunksCount = 1;
        long readBytes = 0;
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            while (true) {
                //read char, check if it is the end of file
                int currentReadInt = reader.read();
                if (currentReadInt < 0) {
                    //if file ended, write lines to final chunk
                    chunkLines.add(buffer.toString());
                    sortChunkAndWriteToDisk(chunkLines, path.getParent(), chunksCount);
                    break;
                }

                if (readBytes > CHUNK_LIMIT_BYTES) {
                    throw new RuntimeException("Line length is bigger than memory limit");
                }

                //check if we have reached the chunk limit.
                //if reached - save lines to chunk and start a new one
                if (readBytes > 0 && readBytes % CHUNK_LIMIT_BYTES == 0) {
                    sortChunkAndWriteToDisk(chunkLines, path.getParent(), chunksCount);
                    chunkLines.clear();
                    chunksCount++;
                    readBytes = 0;
                }

                char currentChar = (char) currentReadInt;
                //check if the char is the end of the line
                if (currentChar == '\n' || currentChar == '\r') {
                    if (buffer.length() == 0) {
                        //buffer is empty, skip the char
                        continue;
                    }
                    chunkLines.add(buffer.toString());
                    buffer = new StringBuffer();
                } else {
                    //regular char - just append to buffer
                    buffer.append(currentChar);
                    readBytes++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Split into " + chunksCount + " chunks");
        return chunksCount;
    }

    private static void sortChunkAndWriteToDisk(List<String> chunkLines, Path folderPath, int chunkNumber) throws IOException {
        int chunkFileIndex = chunkNumber - 1;
        //create file
        Path chunkFilePath = folderPath.resolve(CHUNK_FILE_NAME + chunkFileIndex);
        createOrReplaceFile(chunkFilePath);
        File chunkFile = new File(chunkFilePath.toUri());
        try (FileWriter fileWriter = new FileWriter(chunkFile)) {

            //sort lines and write them to chunk file
            Collections.sort(chunkLines);
            for (int i = 0; i < chunkLines.size(); i++) {
                if (i != 0) {
                    fileWriter.write(System.lineSeparator());
                }
                fileWriter.write(chunkLines.get(i));
            }
        }
        System.out.println("Saved chunk " + chunkFileIndex + " with " + chunkLines.size() + " lines");
    }

    //k-way merge
    private static Path mergeChunks(Path folderPath, int chunksCount) throws IOException {
        //if there's only one chunk - just rename it to result
        Path resultFilePath = folderPath.resolve(RESULT_FILE_NAME);
        if (chunksCount == 1) {
            System.out.println("Only one chunk, renaming it to result instead of merging.");
            Path chunkPath = folderPath.resolve(CHUNK_FILE_NAME + 0);
            Files.move(chunkPath, resultFilePath, StandardCopyOption.REPLACE_EXISTING);
            return resultFilePath;
        }

        System.out.println("Merging " + chunksCount + " chunks");

        File resultFile = createOrReplaceFile(resultFilePath);//initial file can be used instead of new one

        BufferedReader[] readers = new BufferedReader[chunksCount];
        String[] fileLines = new String[chunksCount];
        try (FileWriter resultWriter = new FileWriter(resultFile)) {
            //init readers
            for (int i = 0; i < chunksCount; i++) {
                Path chunkFilePath = folderPath.resolve(CHUNK_FILE_NAME + i);
                readers[i] = Files.newBufferedReader(chunkFilePath);
            }

            //init first lines
            for (int i = 0; i < chunksCount; i++) {
                fileLines[i] = readers[i].readLine();
            }

            //merge and write result file
            long linesWritten = 0;
            while (true) {
                //find min
                String min = fileLines[0];
                int minReaderIndex = 0;//index of reader where min value is found

                for (int i = 1; i < chunksCount; i++) {
                    if (fileLines[i] == null) {
                        //this file ended, skip it
                        continue;
                    }
                    if (min == null || fileLines[i].compareTo(min) < 0) {
                        min = fileLines[i];
                        minReaderIndex = i;
                    }
                }
                //if no min found = all files ended
                if (min == null) {
                    System.out.println("Ended merging");
                    break;
                }
                //write line to result
                if (linesWritten > 0) {
                    resultWriter.write(System.lineSeparator());
                }
                resultWriter.write(min);
                linesWritten++;
                //read next line in place of 'min'-line we already processed
                fileLines[minReaderIndex] = readers[minReaderIndex].readLine();
            }
        } finally {
            //close readers
            for (int i = 0; i < chunksCount; i++) {
                readers[i].close();
            }
        }

        //delete chunks
        for (int i = 0; i < chunksCount; i++) {
            Path chunkFilePath = folderPath.resolve(CHUNK_FILE_NAME + i);
            Files.delete(chunkFilePath);
            System.out.println("Deleted chunk " + i);
        }
        return resultFilePath;
    }


    private static void validateFileSorted(Path filePath) throws IOException {
        BufferedReader reader = Files.newBufferedReader(filePath);
        String prevLine = reader.readLine();
        String nextLine = reader.readLine();
        int prevLineNumber = 1;
        while (nextLine != null) {
            if (prevLine.compareTo(nextLine) > 0) {
                System.err.println("Invalid order! On lines " + prevLineNumber);
                System.err.println("First = " + prevLine);
                System.err.println("Next = " + nextLine);
                return;
            }
            prevLineNumber++;
            prevLine = nextLine;
            nextLine = reader.readLine();
        }
        reader.close();
        System.out.println("File sorted correctly.");
    }

}

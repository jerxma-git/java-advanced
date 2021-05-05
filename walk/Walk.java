package info.kgeorgiy.ja.zheromskii.walk;

import java.io.*;
import java.nio.file.*;

public class Walk {
    private static void printE(final Exception e, final String message) {
        System.err.println(message);
        System.err.println(e.getMessage());
    }

    public static void main(final String[] args) {
        if (args == null || args.length != 2) {
            System.err.println("Usage: java info.kgeorgiy.ja.zheromskii.walk.Walk input_file output_file");
            return;
        }

        if (args[0] == null || args[1] == null) {
            System.err.println("Invalid arguments");
            return;
        }

        Path inputPath = null;
        Path outputPath = null;
        try {
            inputPath = Path.of(args[0]);
        } catch (final InvalidPathException e) {
            printE(e, "Invalid path to the input file.");
            return;
        }

        try {
            outputPath = Path.of(args[1]);
        } catch (final InvalidPathException e) {
            printE(e, "Invalid path to the output file.");
            return;
        }

        try {
            final Path outputParentPath = outputPath.getParent();
            if (outputParentPath != null) {
                Files.createDirectories(outputParentPath);
            }
        } catch (final FileAlreadyExistsException e) {
            printE(e, "Output file's path conflicts with existing files");
            return;
        }  catch (final IOException e) {
            printE(e, "Couldn't create parent directories for output file " + args[1]);
            return;
        } 

        try (final BufferedReader inputReader = Files.newBufferedReader(inputPath)) {
            try (final BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
                String fileName;
                // :NOTE: Сообщение?
                while ((fileName = inputReader.readLine()) != null) {
                    final long hash = hashSumPJW(fileName);
                    final String str = String.format("%016x %s%n", hash, fileName);
                    try {
                        writer.write(str);
                    } catch (final IOException e) {
                        System.err.println("I/O error while writing to file " + fileName);
                        System.err.println(e.getMessage());
                    }
                }
            } catch (final AccessDeniedException e) {
                printE(e, "Access denied to output file");
            } catch (final FileNotFoundException e) {
                printE(e, "Output file not found and couldn't be created");
            } catch (final IOException e) {
                printE(e, "I/O error in opening output file");
            }
        } catch (final NoSuchFileException e) {
            printE(e, "Input file doesn't exist");
        } catch (final AccessDeniedException e) {
            printE(e, "Access denied to input file");
        } catch (final FileNotFoundException e) {
            printE(e, "Input file couldn't be found");
        } catch (final IOException e) {
                printE(e, "I/O error in opening input file");
        } 
        
    }

    static long hashSumPJW(final String fileName) {
        try (final InputStream is = Files.newInputStream(Path.of(fileName), StandardOpenOption.READ)) {
            final int BITS = 64;
            final int HIGH = BITS / 8;
            final int LOW = BITS - HIGH;
            long high;
            long h = 0;
            int nextByte;
            
            while ((nextByte = is.read()) >= 0) {
                h = (h << HIGH) + nextByte;
                if ((high = h >> LOW << LOW) != 0) {
                    h ^= high >> (BITS * 3 / 4);
                    h &= ~high;
                }
            }
            return h;
            
        } catch (final IOException e) {
            printE(e, "I/O error while reading file " + fileName);
            return 0;
        } catch (final InvalidPathException e) {
            printE(e, "Invalid path: " + fileName);
            return 0;
        }
    }
}
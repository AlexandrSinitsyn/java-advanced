package info.kgeorgiy.ja.sinitsyn.walk;

import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public abstract class AbstractWalk {

    private static final int BUFFER_SIZE = 16384;
    private static final String DEFAULT_RESULT = "0".repeat(40);
    private MessageDigest digest;

    protected String getSHA1(final Path path) {
        String res = "";

        if (path != null && !Files.notExists(path)) {
            try (final InputStream in = Files.newInputStream(path)) {
                int n;
                byte[] buffer = new byte[BUFFER_SIZE];
                while ((n = in.read(buffer)) != -1) {
                    digest.update(buffer, 0, n);
                }

                res = new BigInteger(1, digest.digest()).toString(16);
            } catch (final IOException e) {
                throw new WalkException(String.format("Failed to get SHA1 of '%s' because of:\n\t%s: %s", path, e.getClass().getName(), e.getMessage()));
            }
        }

        return String.format("%40s", res).replace(' ', '0');
    }

    protected abstract void processDirectory(final Path path, final BufferedWriter out);

    protected void getHashByPath(final Path path, final BufferedWriter out) {
        try {
            if (Files.isDirectory(path)) {
                processDirectory(path, out);
            } else {
                try {
                    out.write(getSHA1(path) + " " + path);
                } catch (final WalkException ignored) {
                    out.write(DEFAULT_RESULT + " " + path);
                }
                out.newLine();
            }
        } catch (final IOException e) {
            // :NOTE: Сообщение
            throw new WalkException("No access to write to output file");
        }
    }

    // :NOTE: Исключения
    protected void run(final String[] args) {
        if (args == null) {
            System.err.println("Unsupported for 'null' args");
            return;
        }

        if (args.length != 2) {
            System.err.println("Unsupported for this count of file names: " + args.length);
            return;
        }

        final String inputFile = args[0];
        final String outputFile = args[1];

        if (inputFile == null || outputFile == null) {
            System.err.println("Unsupported for 'null'-named files: " + (inputFile == null ? "input" : "output") + " file");
            return;
        }

        final Path inputPath;
        final Path outputPath;
        try {
            inputPath = Paths.get(inputFile);
            outputPath = Paths.get(outputFile);
        } catch (final InvalidPathException e) {
            System.err.println("Invalid path (no such file): " + e.getMessage());
            return;
        }

        try {
            digest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Invalid algorithm of hashing: " + e.getMessage());
        }

        processOnValidArgs(inputPath, outputPath, System.err);
    }

    private void processOnValidArgs(final Path inputPath, final Path outputPath, final PrintStream errStream) {
        try {
            final Path parent = outputPath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
        } catch (final IOException ignored) {}

        try (final BufferedReader in = Files.newBufferedReader(inputPath);
             final BufferedWriter out = Files.newBufferedWriter(outputPath)) {
            String line;
            while ((line = in.readLine()) != null) {
                try {

                    getHashByPath(Paths.get(line), out);
                } catch (final InvalidPathException e) {
                    errStream.println("Invalid path: " + e.getMessage());
                    out.write(String.format("%s %s%n", DEFAULT_RESULT, line));
                } catch (final WalkException e) {
                    errStream.println(e.getMessage());
                    out.write(String.format("%s %s%n", DEFAULT_RESULT, line));
                }
            }
        } catch (final IOException e) {
            errStream.println("Exception occurred while writing to file: " + e.getMessage());
        }
    }
}

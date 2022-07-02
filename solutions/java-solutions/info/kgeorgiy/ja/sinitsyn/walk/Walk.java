package info.kgeorgiy.ja.sinitsyn.walk;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Path;

public class Walk extends AbstractWalk {

    @Override
    protected void processDirectory(final Path path, final BufferedWriter out) {
        try {
            out.write(String.format("%s %s%n", getSHA1(path), path));
        } catch (final IOException e) {
            throw new WalkException("Something went wrong while writing", e);
        }
    }

    public static void main(final String[] args) {
        new Walk().run(args);
    }
}

package walk;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class RecursiveWalk extends AbstractWalk {

    @Override
    protected void processDirectory(final Path path, final BufferedWriter out) {
        try (final var dir = Files.newDirectoryStream(path)) {
            for (final Path entry : dir) {
                getHashByPath(entry, out);
            }
        } catch (final IOException e) {
            throw new WalkException(String.format("Something went wrong while searching here '%s':\n\t%s", path, e.getMessage()), e);
        }
    }

    public static void main(final String[] args) {
        new RecursiveWalk().run(args);
    }
}

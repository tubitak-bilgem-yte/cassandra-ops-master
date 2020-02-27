package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.copy.impl.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicLong;

import static java.nio.file.FileVisitResult.CONTINUE;

/**
 * Various utility methods for creating, deleting, zipping and calculating sizes of files / directories.
 */
public class FileUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtils.class);

    /**
     * Calculates the size of a path and its subcontents recursively.
     */
    public static long size(final Path path) throws IOException {
        DirectorySizeVisitor directorySizeVisitor = new DirectorySizeVisitor();
        Files.walkFileTree(path, directorySizeVisitor);
        return directorySizeVisitor.getSize();
    }

    /**
     * Deletes given path and all of its subfiles and subdirectories.
     */
    public static void delete(final Path path) throws IOException {
        DeleteVisitor deleteVisitor = new DeleteVisitor();
        Files.walkFileTree(path, deleteVisitor);
    }


    private static class DirectorySizeVisitor extends SimpleFileVisitor<Path> {
        private static final String COULD_NOT_VISIT_FILE = "Could not visit file %s while calculating file tree size";

        final AtomicLong size = new AtomicLong(0);

        @Override
        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) {
            size.addAndGet(attrs.size());
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(final Path file, final IOException e) throws IOException {
            FileUtils.LOGGER.error(String.format(DirectorySizeVisitor.COULD_NOT_VISIT_FILE, file));
            throw e;
        }

        public long getSize() {
            return size.get();
        }
    }

    private static class DeleteVisitor extends SimpleFileVisitor<Path> {

        private static final String COULD_NOT_DELETE_FILE = "Could not delete file %s while recursively deleting file tree.";
        private static final String COULD_NOT_VISIT_FILE_WHILE_DELETING = "Could not visit file %s while recursively deleting  file tree";
        private static final String COULD_NOT_DELETE_DIRECTORY_ITERATION_ERROR = "Could not delete directory %s because iteration of the directory completed prematurely";
        private static final String COULD_NOT_DELETE_DIRECTORY = "Could not delete directory %s while recursively deleting file tree";

        private final boolean onErrorContinue;

        public DeleteVisitor() {
            this.onErrorContinue = true;
        }

        @Override
        public FileVisitResult visitFile(final Path path, final BasicFileAttributes basicFileAttributes) throws IOException {
            try {
                Files.delete(path);
            } catch (final IOException e) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                FileUtils.LOGGER.error(
                        String.format(DeleteVisitor.COULD_NOT_DELETE_FILE, path.toString()),
                        e);
                if (!onErrorContinue) {
                    throw e;
                }
            }
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(final Path path, final IOException e) throws IOException {
            FileUtils.LOGGER.error(
                    String.format(DeleteVisitor.COULD_NOT_VISIT_FILE_WHILE_DELETING, path.toString()),
                    e);
            if (!onErrorContinue) {
                throw e;
            }
            return CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(final Path path, IOException e) throws IOException {
            if (e != null) {
                FileUtils.LOGGER.error(
                        String.format(DeleteVisitor.COULD_NOT_DELETE_DIRECTORY_ITERATION_ERROR, path.toString()),
                        e);

            } else {
                try {
                    Files.delete(path);
                } catch (final IOException e1) {
                    e = e1;
                    FileUtils.LOGGER.error(
                            String.format(DeleteVisitor.COULD_NOT_DELETE_DIRECTORY, path.toString()),
                            e1);
                }
            }
            if (e != null && !onErrorContinue) {
                throw e;
            }
            return CONTINUE;
        }
    }
}
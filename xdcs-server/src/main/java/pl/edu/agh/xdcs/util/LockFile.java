package pl.edu.agh.xdcs.util;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Instance of this class represents a lock file, i.e. a file which is used to lock
 * a resource on the filesystem.
 *
 * @author Kamil Jarosz
 */
public class LockFile implements AutoCloseable {
    private final Path lockFile;
    private final FileChannel channel;
    private final FileLock lock;

    private LockFile(FileChannel channel, Path lockFile) throws IOException, LockFailedException {
        this.lockFile = lockFile;
        this.channel = channel;
        try {
            this.lock = channel.lock();
        } catch (OverlappingFileLockException e) {
            channel.close();
            throw new LockFailedException(e);
        }
    }

    public static LockFile newLockFile(Path path) throws LockFailedException {
        try {
            if (!Files.exists(path)) Files.createFile(path);
            return new LockFile(FileChannel.open(path,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.APPEND), path);
        } catch (IOException e) {
            throw new LockFailedException(e);
        }
    }

    @Override
    public void close() {
        try {
            lock.release();
            channel.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            try {
                Files.delete(lockFile);
            } catch (IOException ignored) {

            }
        }
    }

    public static class LockFailedException extends Exception {
        public LockFailedException(Throwable cause) {
            super(cause);
        }
    }
}

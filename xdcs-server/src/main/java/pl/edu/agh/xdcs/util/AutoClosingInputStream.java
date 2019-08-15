package pl.edu.agh.xdcs.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Kamil Jarosz
 */
public class AutoClosingInputStream extends FilterInputStream {
    private boolean closed = false;
    private boolean closedExplicitly = false;

    AutoClosingInputStream(InputStream delegate) {
        super(delegate);
    }

    public static InputStream of(InputStream is) {
        return new AutoClosingInputStream(is);
    }

    @Override
    public int read() throws IOException {
        if (closedImplicitlyOnly()) {
            return -1;
        }

        int read = super.read();
        if (read == -1) {
            closeImplicitly();
        }

        return read;
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (len != 0 && closedImplicitlyOnly()) {
            return -1;
        }

        int read = super.read(b, off, len);
        if (read == -1) {
            closeImplicitly();
        }

        return read;
    }

    @Override
    public void close() throws IOException {
        closeExplicitly();
    }

    @Override
    public synchronized void mark(int readlimit) {

    }

    @Override
    public synchronized void reset() throws IOException {
        throw new IOException("mark/reset not supported");
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    private boolean closedImplicitlyOnly() {
        return closed && !closedExplicitly;
    }

    private void closeExplicitly() throws IOException {
        closedExplicitly = true;
        closeImplicitly();
    }

    private void closeImplicitly() throws IOException {
        if (!closed) {
            closed = true;
            super.close();
        }
    }
}

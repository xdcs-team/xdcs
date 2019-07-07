package pl.edu.agh.xdcs.grpc.tunnel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * @author Kamil Jarosz
 */
class ProxyServer implements AutoCloseable {
    private final ServerSocket proxyServer;
    private final Executor executor;
    private Socket proxyConnection;

    private Consumer<byte[]> chunkListener = c -> {

    };

    private Consumer<Throwable> errorListener = t -> {

    };

    private Runnable completionListener = () -> {

    };

    ProxyServer(Executor executor) throws IOException {
        this.executor = executor;
        this.proxyServer = new ServerSocket(0);
    }

    void setChunkListener(Consumer<byte[]> chunkListener) {
        this.chunkListener = Objects.requireNonNull(chunkListener);
    }

    void setCompletionListener(Runnable completionListener) {
        this.completionListener = Objects.requireNonNull(completionListener);
    }

    void setErrorListener(Consumer<Throwable> errorListener) {
        this.errorListener = Objects.requireNonNull(errorListener);
    }

    int getLocalPort() {
        return proxyServer.getLocalPort();
    }

    void waitForConnection() throws IOException {
        this.proxyConnection = proxyServer.accept();
        this.executor.execute(() -> {
            try {
                while (!Thread.interrupted()) {
                    byte[] bytes = readChunk();

                    if (bytes == null) {
                        completionListener.run();
                        break;
                    }

                    chunkListener.accept(bytes);
                }
            } catch (IOException e) {
                errorListener.accept(e);
            }
        });

    }

    private byte[] readChunk() throws IOException {
        InputStream inputStream = proxyConnection.getInputStream();
        int first = inputStream.read();
        if (first == -1) {
            return null;
        }

        int length = inputStream.available();
        byte[] bytes = new byte[length + 1];
        bytes[0] = (byte) first;
        int read = inputStream.read(bytes, 1, length);

        if (read < length) {
            return Arrays.copyOf(bytes, read);
        } else {
            return bytes;
        }
    }

    void sendChunk(byte[] bytes) throws IOException {
        OutputStream outputStream = proxyConnection.getOutputStream();
        outputStream.write(bytes);
        outputStream.flush();
    }

    @Override
    public void close() throws IOException {
        if (proxyConnection != null) proxyConnection.close();

        proxyServer.close();
    }
}

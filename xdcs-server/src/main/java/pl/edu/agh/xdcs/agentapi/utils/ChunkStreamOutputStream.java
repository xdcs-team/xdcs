package pl.edu.agh.xdcs.agentapi.utils;

import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;
import pl.edu.agh.xdcs.api.Chunk;

import java.io.OutputStream;

/**
 * @author Kamil Jarosz
 */
public class ChunkStreamOutputStream extends OutputStream {
    private final StreamObserver<Chunk> responseObserver;

    public ChunkStreamOutputStream(StreamObserver<Chunk> responseObserver) {
        this.responseObserver = responseObserver;
    }

    @Override
    public void write(int b) {
        sendChunk(ByteString.copyFrom(new byte[]{(byte) b}));
    }

    @Override
    public void write(byte[] b) {
        sendChunk(ByteString.copyFrom(b));
    }

    @Override
    public void write(byte[] b, int off, int len) {
        sendChunk(ByteString.copyFrom(b, off, len));
    }

    @Override
    public void close() {
        responseObserver.onCompleted();
    }

    private void sendChunk(ByteString bytes) {
        responseObserver.onNext(Chunk.newBuilder()
                .setContent(bytes)
                .build());
    }
}

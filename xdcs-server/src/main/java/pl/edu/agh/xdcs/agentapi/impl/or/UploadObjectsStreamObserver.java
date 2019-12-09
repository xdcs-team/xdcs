package pl.edu.agh.xdcs.agentapi.impl.or;

import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.xdcs.agentapi.utils.GrpcServiceException;
import pl.edu.agh.xdcs.api.Chunk;
import pl.edu.agh.xdcs.api.OkResponse;
import pl.edu.agh.xdcs.or.ObjectRepository;

import javax.enterprise.concurrent.ManagedThreadFactory;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.UncheckedIOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author Kamil Jarosz
 */
public class UploadObjectsStreamObserver implements StreamObserver<Chunk> {
    private static final Logger logger = LoggerFactory.getLogger(UploadObjectsStreamObserver.class);

    private final StreamObserver<OkResponse> responseObserver;
    private final ObjectRepository objectRepository;
    private final ZipInputStream inputStream;
    private final PipedOutputStream pipedOutput;

    UploadObjectsStreamObserver(
            ManagedThreadFactory threadFactory,
            ObjectRepository objectRepository,
            StreamObserver<OkResponse> responseObserver) {
        this.objectRepository = objectRepository;
        this.responseObserver = responseObserver;
        this.pipedOutput = new PipedOutputStream();
        try {
            this.inputStream = new ZipInputStream(new PipedInputStream(pipedOutput));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        threadFactory.newThread(this::receiveAsync).start();
    }

    private void receiveAsync() {
        try {
            while (true) {
                ZipEntry nextEntry = inputStream.getNextEntry();
                if (nextEntry == null) return;

                String objectId = nextEntry.getName();
                String storedObjectId = objectRepository.store(inputStream);

                if (!storedObjectId.equals(objectId)) {
                    responseObserver.onError(new GrpcServiceException(
                            "Object IDs don't match: " + objectId + ", " + storedObjectId));
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                logger.error("Exception while closing input stream", e);
            }
        }
    }

    @Override
    public void onNext(Chunk value) {
        try {
            pipedOutput.write(value.getContent().toByteArray());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void onError(Throwable t) {
        onCompleted();
    }

    @Override
    public void onCompleted() {
        responseObserver.onNext(OkResponse.newBuilder().build());
        responseObserver.onCompleted();
        try {
            pipedOutput.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}

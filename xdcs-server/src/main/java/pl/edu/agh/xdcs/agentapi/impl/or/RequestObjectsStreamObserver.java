package pl.edu.agh.xdcs.agentapi.impl.or;

import io.grpc.stub.StreamObserver;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ChunkedOutputStream;
import pl.edu.agh.xdcs.agentapi.utils.ChunkStreamOutputStream;
import pl.edu.agh.xdcs.api.Chunk;
import pl.edu.agh.xdcs.api.ObjectIds;
import pl.edu.agh.xdcs.or.ObjectRepository;

import javax.enterprise.concurrent.ManagedThreadFactory;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author Kamil Jarosz
 */
class RequestObjectsStreamObserver implements StreamObserver<ObjectIds> {
    private final BlockingQueue<Command> commands = new LinkedBlockingQueue<>();
    private final ZipOutputStream outputStream;
    private final ObjectRepository objectRepository;

    RequestObjectsStreamObserver(
            ManagedThreadFactory threadFactory,
            ObjectRepository objectRepository,
            StreamObserver<Chunk> responseObserver) {
        this.objectRepository = objectRepository;
        this.outputStream = new ZipOutputStream(
                new ChunkedOutputStream(
                        new ChunkStreamOutputStream(responseObserver)));

        threadFactory.newThread(() -> {
            try {
                sendAsync();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }).start();
    }

    private void sendAsync() throws IOException {
        try {
            while (true) {
                Command cmd = commands.take();
                if (cmd.finish) return;
                sendObject(cmd.objectId);
            }
        } catch (InterruptedException ignored) {
        } finally {
            outputStream.close();
        }
    }

    private void sendObject(String objectId) throws IOException {
        outputStream.putNextEntry(new ZipEntry(objectId));
        IOUtils.copy(objectRepository.cat(objectId), outputStream);
        outputStream.closeEntry();
    }

    @Override
    public void onNext(ObjectIds value) {
        commands.addAll(value.getObjectIdsList()
                .stream()
                .map(Command::sendObject)
                .collect(Collectors.toList()));
    }

    @Override
    public void onError(Throwable t) {
        commands.add(Command.finish());
    }

    @Override
    public void onCompleted() {
        commands.add(Command.finish());
    }

    @Getter
    @AllArgsConstructor
    private static class Command {
        private final String objectId;
        private final boolean finish;

        private static Command sendObject(String objectId) {
            return new Command(objectId, false);
        }

        private static Command finish() {
            return new Command(null, true);
        }
    }
}

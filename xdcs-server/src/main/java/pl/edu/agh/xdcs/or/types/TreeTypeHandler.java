package pl.edu.agh.xdcs.or.types;

import com.fasterxml.jackson.databind.ObjectMapper;
import pl.edu.agh.xdcs.or.ObjectKey;
import pl.edu.agh.xdcs.or.ObjectRepositoryTypeHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.stream.Stream;

/**
 * @author Kamil Jarosz
 */
@GlobalTypeHandler
public class TreeTypeHandler implements ObjectRepositoryTypeHandler<Tree> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Class<Tree> getRepresentation() {
        return Tree.class;
    }

    @Override
    public Tree read(InputStream file) throws IOException {
        return objectMapper.readValue(file, Tree.class);
    }

    @Override
    public void write(Tree object, OutputStream file) throws IOException {
        objectMapper.writeValue(file, object);
    }

    @Override
    public Stream<ObjectKey> dependencies(Tree object) {
        return object.getEntries().stream().flatMap(entry -> {
            switch (entry.getMode().getType()) {
                case S_IFLNK:
                case S_IFREG:
                    return Stream.of(ObjectKey.from(entry.getObjectId(), Blob.class));
                case S_IFDIR:
                    return Stream.of(ObjectKey.from(entry.getObjectId(), Tree.class));
                default:
                    return Stream.empty();
            }
        });
    }
}

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
public class DeploymentTypeHandler implements ObjectRepositoryTypeHandler<Deployment> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Class<Deployment> getRepresentation() {
        return Deployment.class;
    }

    @Override
    public Deployment read(InputStream file) throws IOException {
        return objectMapper.readValue(file, Deployment.class);
    }

    @Override
    public void write(Deployment object, OutputStream file) throws IOException {
        objectMapper.writeValue(file, object);
    }

    @Override
    public Stream<ObjectKey> dependencies(Deployment object) {
        return Stream.of(ObjectKey.from(object.getRoot(), Tree.class));
    }
}

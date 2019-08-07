package pl.edu.agh.xdcs.or.types;

import com.fasterxml.jackson.databind.ObjectMapper;
import pl.edu.agh.xdcs.or.ObjectRepositoryTypeHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Kamil Jarosz
 */
public class TreeTypeHandler implements ObjectRepositoryTypeHandler<Tree> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getTypeName() {
        return Tree.TYPE_NAME;
    }

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
}

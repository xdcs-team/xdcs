package pl.edu.agh.xdcs.objectmapper;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Kamil Jarosz
 */
public class StreamSerializer extends JsonSerializer<Stream> {
    @SuppressWarnings("unchecked")
    @Override
    public void serialize(Stream t, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeObject(t.collect(Collectors.toList()));
    }
}

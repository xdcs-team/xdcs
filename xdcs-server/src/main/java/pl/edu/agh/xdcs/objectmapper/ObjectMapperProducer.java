package pl.edu.agh.xdcs.objectmapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import java.util.stream.Stream;

/**
 * @author Kamil Jarosz
 */
@ApplicationScoped
public class ObjectMapperProducer {
    private final ObjectMapper mapper;

    public ObjectMapperProducer() {
        this.mapper = createObjectMapper();
    }

    @Produces
    public ObjectMapper getObjectMapper() {
        return mapper.copy();
    }

    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(Stream.class, new StreamSerializer());
        mapper.registerModule(module);
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return mapper;
    }
}

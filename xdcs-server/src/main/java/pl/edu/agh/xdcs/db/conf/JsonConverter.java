package pl.edu.agh.xdcs.db.conf;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.persistence.AttributeConverter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;

/**
 * @author Kamil Jarosz
 */
public class JsonConverter<T> implements AttributeConverter<Object, String> {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TypeReference<T> type;
    private final Class<T> clazz;

    public JsonConverter(Class<T> clazz) {
        this.type = null;
        this.clazz = Objects.requireNonNull(clazz);
    }

    public JsonConverter(TypeReference<T> type) {
        this.type = Objects.requireNonNull(type);
        this.clazz = null;
    }

    @Override
    public String convertToDatabaseColumn(Object attribute) {
        if (attribute == null) return null;

        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Object convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;

        try {
            if (clazz != null) {
                return objectMapper.readValue(dbData, clazz);
            } else {
                return objectMapper.readValue(dbData, type);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}

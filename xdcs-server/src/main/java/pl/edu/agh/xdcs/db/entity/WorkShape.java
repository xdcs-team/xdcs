package pl.edu.agh.xdcs.db.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import javax.persistence.AttributeConverter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.List;

@Getter
@Builder
@EqualsAndHashCode
public class WorkShape {
    private int x;
    private int y;
    private int z;

    public static WorkShape fromList(List<Integer> shape) {
        return WorkShape.builder()
                .x(shape.get(0))
                .y(shape.get(1))
                .z(shape.get(2))
                .build();
    }

    public List<Integer> asList() {
        return Arrays.asList(x, y, z);
    }

    public static class Converter implements AttributeConverter<WorkShape, byte[]> {
        private final ObjectMapper objectMapper = new ObjectMapper();

        @Override
        public byte[] convertToDatabaseColumn(WorkShape integers) {
            if (integers == null) return null;

            try {
                return objectMapper.writeValueAsBytes(integers.asList());
            } catch (JsonProcessingException e) {
                throw new UncheckedIOException(e);
            }
        }

        @Override
        public WorkShape convertToEntityAttribute(byte[] bytes) {
            if (bytes == null) return null;

            try {
                List<Integer> integers = objectMapper.readValue(bytes, new TypeReference<List<Integer>>() { });
                return WorkShape.fromList(integers);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }
}

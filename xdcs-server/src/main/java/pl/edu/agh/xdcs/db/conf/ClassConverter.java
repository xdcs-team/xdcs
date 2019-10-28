package pl.edu.agh.xdcs.db.conf;

import javax.persistence.AttributeConverter;

/**
 * @author Kamil Jarosz
 */
public class ClassConverter implements AttributeConverter<Class<?>, String> {
    @Override
    public String convertToDatabaseColumn(Class<?> attribute) {
        if (attribute.isPrimitive()) throw new IllegalStateException("Primitives are not supported");
        return attribute.getName();
    }

    @Override
    public Class<?> convertToEntityAttribute(String dbData) {
        try {
            return Class.forName(dbData);
        } catch (ClassNotFoundException e) {
            throw new MissingClassError(e);
        }
    }

    public static class MissingClassError extends Error {
        public MissingClassError(Throwable cause) {
            super(cause);
        }
    }
}

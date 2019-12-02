package pl.edu.agh.xdcs.restapi.util;

import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

/**
 * @author Kamil Jarosz
 */
@Provider
public class JavaTimeParamConverterProvider implements ParamConverterProvider {
    @SuppressWarnings("unchecked")
    @Override
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
        if (rawType == OffsetDateTime.class) {
            return (ParamConverter<T>) new ParamConverter<OffsetDateTime>() {
                @Override
                public OffsetDateTime fromString(String value) {
                    return OffsetDateTime.parse(value);
                }

                @Override
                public String toString(OffsetDateTime value) {
                    return value.toString();
                }
            };
        }

        if (rawType == LocalDateTime.class) {
            return (ParamConverter<T>) new ParamConverter<LocalDateTime>() {
                @Override
                public LocalDateTime fromString(String value) {
                    return LocalDateTime.parse(value);
                }

                @Override
                public String toString(LocalDateTime value) {
                    return value.toString();
                }
            };
        }

        if (rawType == LocalDate.class) {
            return (ParamConverter<T>) new ParamConverter<LocalDate>() {
                @Override
                public LocalDate fromString(String value) {
                    return LocalDate.parse(value);
                }

                @Override
                public String toString(LocalDate value) {
                    return value.toString();
                }
            };
        }

        if (rawType == ZonedDateTime.class) {
            return (ParamConverter<T>) new ParamConverter<ZonedDateTime>() {
                @Override
                public ZonedDateTime fromString(String value) {
                    return ZonedDateTime.parse(value);
                }

                @Override
                public String toString(ZonedDateTime value) {
                    return value.toString();
                }
            };
        }

        if (rawType == Instant.class) {
            return (ParamConverter<T>) new ParamConverter<Instant>() {
                @Override
                public Instant fromString(String value) {
                    return Instant.parse(value);
                }

                @Override
                public String toString(Instant value) {
                    return value.toString();
                }
            };
        }

        return null;
    }
}

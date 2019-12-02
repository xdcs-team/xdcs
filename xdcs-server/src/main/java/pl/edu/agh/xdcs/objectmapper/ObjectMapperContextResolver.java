package pl.edu.agh.xdcs.objectmapper;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.inject.Inject;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

/**
 * Used to configure {@link ObjectMapper} used by JAX-RS.
 *
 * @author Kamil Jarosz
 */
@Provider
public class ObjectMapperContextResolver implements ContextResolver<ObjectMapper> {
    @Inject
    private ObjectMapper objectMapper;

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return objectMapper;
    }
}

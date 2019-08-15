package pl.edu.agh.xdcs.restapi.mapper;

import java.util.Collection;
import java.util.stream.Stream;

/**
 * @author Kamil Jarosz
 */
public interface SimpleMapper<M, R> {
    M toModelEntity(R restEntity);

    default Stream<M> toModelEntities(Collection<R> restEntities) {
        return restEntities.stream().map(this::toModelEntity);
    }

    R toRestEntity(M modelEntity);

    default Stream<R> toRestEntities(Collection<M> modelEntities) {
        return modelEntities.stream().map(this::toRestEntity);
    }
}

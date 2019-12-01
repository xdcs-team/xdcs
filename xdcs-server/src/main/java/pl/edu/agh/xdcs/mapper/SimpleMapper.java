package pl.edu.agh.xdcs.mapper;

import java.util.Collection;
import java.util.stream.Stream;

/**
 * @author Kamil Jarosz
 */
public interface SimpleMapper<M, A> {
    M toModelEntity(A apiEntity);

    default Stream<M> toModelEntities(Collection<? extends A> apiEntities) {
        return apiEntities.stream().map(this::toModelEntity);
    }

    A toApiEntity(M modelEntity);

    default Stream<A> toApiEntities(Collection<? extends M> modelEntities) {
        return modelEntities.stream().map(this::toApiEntity);
    }
}

package pl.edu.agh.xdcs.mapper;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author Kamil Jarosz
 */
public class EnumMapper<M extends Enum<M>, R extends Enum<R>> implements SimpleMapper<M, R> {
    private final Function<M, R> modelToApi;
    private final Function<R, M> apiToModel;

    protected EnumMapper(Map<M, R> mapping) {
        this(HashBiMap.create(mapping));
    }

    protected EnumMapper(BiMap<M, R> mapping) {
        this(mapping::get, mapping.inverse()::get);
    }

    protected EnumMapper(Function<M, R> modelToApi, Function<R, M> apiToModel) {
        this.modelToApi = modelToApi;
        this.apiToModel = apiToModel;
    }

    public static <A extends Enum<A>, B extends Enum<B>> EnumMapper<A, B> forMapping(Map<A, B> mapping) {
        return new EnumMapper<>(mapping);
    }

    public static <A extends Enum<A>, B extends Enum<B>> EnumMapper<A, B> forMapping(BiMap<A, B> mapping) {
        return new EnumMapper<>(mapping);
    }

    @Override
    public M toModelEntity(R apiEntity) {
        if (apiEntity == null) return null;
        return Optional.ofNullable(apiToModel.apply(apiEntity))
                .orElseThrow(UnsatisfiedMappingException::new);
    }

    @Override
    public R toApiEntity(M modelEntity) {
        if (modelEntity == null) return null;
        return Optional.ofNullable(modelToApi.apply(modelEntity))
                .orElseThrow(UnsatisfiedMappingException::new);
    }
}

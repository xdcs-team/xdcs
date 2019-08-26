package pl.edu.agh.xdcs.restapi.mapper;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author Kamil Jarosz
 */
public class EnumMapper<M extends Enum<M>, R extends Enum<R>> implements SimpleMapper<M, R> {
    private final Function<M, R> modelToRest;
    private final Function<R, M> restToModel;

    protected EnumMapper(Function<M, R> modelToRest, Function<R, M> restToModel) {
        this.modelToRest = modelToRest;
        this.restToModel = restToModel;
    }

    public static <A extends Enum<A>, B extends Enum<B>> EnumMapper<A, B> forMapping(Map<A, B> mapping) {
        return forMapping(HashBiMap.create(mapping));
    }

    public static <A extends Enum<A>, B extends Enum<B>> EnumMapper<A, B> forMapping(BiMap<A, B> mapping) {
        BiMap<B, A> reverseMapping = mapping.inverse();
        return new EnumMapper<>(mapping::get, reverseMapping::get);
    }

    @Override
    public M toModelEntity(R restEntity) {
        if (restEntity == null) return null;
        return Optional.ofNullable(restToModel.apply(restEntity))
                .orElseThrow(UnsatisfiedMappingException::new);
    }

    @Override
    public R toRestEntity(M modelEntity) {
        if (modelEntity == null) return null;
        return Optional.ofNullable(modelToRest.apply(modelEntity))
                .orElseThrow(UnsatisfiedMappingException::new);
    }
}

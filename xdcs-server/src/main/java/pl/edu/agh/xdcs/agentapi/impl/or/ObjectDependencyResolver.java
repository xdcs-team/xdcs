package pl.edu.agh.xdcs.agentapi.impl.or;

import io.grpc.stub.StreamObserver;
import pl.edu.agh.xdcs.api.DependencyResolutionRequest;
import pl.edu.agh.xdcs.api.ObjectIds;
import pl.edu.agh.xdcs.api.ObjectType;
import pl.edu.agh.xdcs.or.ObjectBase;
import pl.edu.agh.xdcs.or.ObjectKey;
import pl.edu.agh.xdcs.or.ObjectRepository;
import pl.edu.agh.xdcs.or.types.Blob;
import pl.edu.agh.xdcs.or.types.Deployment;
import pl.edu.agh.xdcs.or.types.Tree;
import pl.edu.agh.xdcs.mapper.UnsatisfiedMappingException;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Kamil Jarosz
 */
public class ObjectDependencyResolver {
    @Inject
    private ObjectRepository objectRepository;

    public ObjectIds resolveDependencies(DependencyResolutionRequest request, StreamObserver<ObjectIds> responseObserver) {
        Set<String> dependencies = new HashSet<>();

        int depth = request.getDepth();
        request.getObjectKeysList()
                .forEach(objectKey -> resolveDependencies(mapObjectKey(objectKey), dependencies, depth));

        return ObjectIds.newBuilder()
                .addAllObjectIds(dependencies)
                .build();
    }

    private void resolveDependencies(ObjectKey objectKey, Set<String> dependencies, int depth) {
        if (depth == 0) return;

        objectRepository.dependenciesFor(objectKey.getObjectId(), objectKey.getType())
                .forEach(key -> {
                    dependencies.add(key.getObjectId());
                    resolveDependencies(key, dependencies, depth - 1);
                });
    }

    private ObjectKey mapObjectKey(pl.edu.agh.xdcs.api.ObjectKey objectKey) {
        return ObjectKey.from(objectKey.getObjectId(), mapObjectType(objectKey.getObjectType()));
    }

    private Class<? extends ObjectBase> mapObjectType(ObjectType type) {
        switch (type) {
            case BLOB:
                return Blob.class;
            case TREE:
                return Tree.class;
            case DEPLOYMENT:
                return Deployment.class;
            case UNRECOGNIZED:
            default:
                throw new UnsatisfiedMappingException();
        }
    }
}

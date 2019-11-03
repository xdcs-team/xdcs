package pl.edu.agh.xdcs.agentapi.or;

import io.grpc.stub.StreamObserver;
import pl.edu.agh.xdcs.api.Chunk;
import pl.edu.agh.xdcs.api.DependencyResolutionRequest;
import pl.edu.agh.xdcs.api.ObjectIds;
import pl.edu.agh.xdcs.api.ObjectRepositoryGrpc;
import pl.edu.agh.xdcs.grpc.Service;
import pl.edu.agh.xdcs.or.ObjectRepository;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedThreadFactory;
import javax.inject.Inject;

/**
 * @author Kamil Jarosz
 */
@Service
public class ObjectRepositoryService extends ObjectRepositoryGrpc.ObjectRepositoryImplBase {
    @Resource
    private ManagedThreadFactory threadFactory;

    @Inject
    private ObjectRepository objectRepository;

    @Inject
    private ObjectDependencyResolver objectDependencyResolver;

    @Override
    public StreamObserver<ObjectIds> requestObjects(StreamObserver<Chunk> responseObserver) {
        return new RequestObjectsStreamObserver(threadFactory, objectRepository, responseObserver);
    }

    @Override
    public void resolveDependencies(DependencyResolutionRequest request, StreamObserver<ObjectIds> responseObserver) {
        objectDependencyResolver.resolveDependencies(request, responseObserver);
    }
}

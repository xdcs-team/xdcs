package pl.edu.agh.xdcs.agentapi.impl.or;

import io.grpc.stub.StreamObserver;
import pl.edu.agh.xdcs.api.Chunk;
import pl.edu.agh.xdcs.api.DependencyResolutionRequest;
import pl.edu.agh.xdcs.api.ObjectIds;
import pl.edu.agh.xdcs.api.ObjectRepositoryGrpc;
import pl.edu.agh.xdcs.api.OkResponse;
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
        ObjectIds objectIds = objectDependencyResolver.resolveDependencies(request, responseObserver);
        responseObserver.onNext(objectIds);
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<Chunk> uploadObjects(StreamObserver<OkResponse> responseObserver) {
        return new UploadObjectsStreamObserver(threadFactory, objectRepository, responseObserver);
    }
}

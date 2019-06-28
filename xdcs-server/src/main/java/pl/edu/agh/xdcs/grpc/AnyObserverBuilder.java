package pl.edu.agh.xdcs.grpc;

import com.google.protobuf.Any;
import io.grpc.stub.StreamObserver;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Kamil Jarosz
 */
public class AnyObserverBuilder {
    private Map<Class<?>, Consumer<?>> matches = new HashMap<>();

    private Consumer<Throwable> onError = t -> {
        throw new RuntimeException("Exception not handled: " + t);
    };

    private Runnable onCompleted = () -> {

    };

    private Consumer<Any> onMatchFailed = any -> {
        throw new RuntimeException("Any match failed for: " + any);
    };

    AnyObserverBuilder() {

    }

    public <T> AnyObserverBuilder match(Class<T> type, Consumer<T> consumer) {
        matches.put(type, consumer);
        return this;
    }

    public AnyObserverBuilder error(Consumer<Throwable> consumer) {
        onError = consumer;
        return this;
    }

    public AnyObserverBuilder complete(Runnable runnable) {
        onCompleted = runnable;
        return this;
    }

    public AnyObserverBuilder matchFailed(Consumer<Any> consumer) {
        onMatchFailed = consumer;
        return this;
    }

    public StreamObserver<Any> build() {
        AnyObserver ret = new AnyObserver(matches, onError, onCompleted, onMatchFailed);
        matches = new HashMap<>();
        return ret;
    }
}

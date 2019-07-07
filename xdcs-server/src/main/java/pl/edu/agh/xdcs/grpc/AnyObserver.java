package pl.edu.agh.xdcs.grpc;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author Kamil Jarosz
 */
public class AnyObserver implements StreamObserver<Any> {
    private static final Logger logger = LoggerFactory.getLogger(AnyObserver.class);

    private final Map<Class<?>, Consumer<?>> matches;
    private final Consumer<Throwable> onError;
    private final Runnable onCompleted;
    private final Consumer<Any> onMatchFailed;

    AnyObserver(Map<Class<?>, Consumer<?>> matches,
                Consumer<Throwable> onError,
                Runnable onCompleted,
                Consumer<Any> onMatchFailed) {
        this.matches = matches;
        this.onError = onError;
        this.onCompleted = onCompleted;
        this.onMatchFailed = onMatchFailed;
    }

    public static AnyObserverBuilder builder() {
        return new AnyObserverBuilder();
    }

    @Override
    public void onNext(Any value) {
        onNext0(value);
    }

    @SuppressWarnings("unchecked")
    private <T extends Message> void onNext0(Any value) {
        Optional<Class<?>> optionalType = AnyUtils.typeOf(value);

        if (!optionalType.isPresent()) {
            logger.error("Cannot deduce Any type: " + value);
            onMatchFailed.accept(value);
            return;
        }

        Class<T> type = (Class<T>) optionalType.get();
        Consumer<T> handler = (Consumer<T>) matches.get(type);

        if (handler == null) {
            logger.error("No handler available for " + type);
            onMatchFailed.accept(value);
            return;
        }

        try {
            handler.accept(value.unpack(type));
        } catch (InvalidProtocolBufferException e) {
            logger.error("The Any type has been deduced to " + type + ", " +
                    "but protobuf thrown an exception when I tried to unpack it", e);
            onMatchFailed.accept(value);
        }
    }

    @Override
    public void onError(Throwable t) {
        onError.accept(t);
    }

    @Override
    public void onCompleted() {
        onCompleted.run();
    }
}

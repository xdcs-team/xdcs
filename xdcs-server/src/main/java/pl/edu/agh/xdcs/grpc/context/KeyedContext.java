package pl.edu.agh.xdcs.grpc.context;

import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Kamil Jarosz
 */
public class KeyedContext<K> implements Context {
    private final Map<K, Scope> scopes = new ConcurrentHashMap<>();
    private final Class<? extends Annotation> scopeAnnotation;

    private final ThreadLocal<AtomicReference<K>> active =
            ThreadLocal.withInitial(() -> new AtomicReference<>(null));

    public KeyedContext(Class<? extends Annotation> scopeAnnotation) {
        this.scopeAnnotation = scopeAnnotation;
    }

    private Scope currentScope() {
        K key = currentKey().get();
        if (key != null) {
            return scopes.computeIfAbsent(key, k -> new Scope());
        } else {
            return Scope.INACTIVE;
        }
    }

    private AtomicReference<K> currentKey() {
        return active.get();
    }

    public K enter(K key) {
        return currentKey().getAndSet(key);
    }

    public void evict(K key) {
        scopes.computeIfPresent(key, (k, scope) -> {
            scope.destroy();
            return null;
        });
    }

    public Optional<K> getCurrentKey() {
        return Optional.ofNullable(currentKey().get());
    }

    @Override
    public <T> T get(Contextual<T> contextual, CreationalContext<T> creationalContext) {
        return currentScope().get(contextual, creationalContext);
    }

    @Override
    public <T> T get(Contextual<T> contextual) {
        return currentScope().get(contextual);
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return scopeAnnotation;
    }
}

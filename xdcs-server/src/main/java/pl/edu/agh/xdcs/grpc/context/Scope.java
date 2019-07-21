package pl.edu.agh.xdcs.grpc.context;

import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Kamil Jarosz
 */
class Scope {
    static final Scope INACTIVE = new Scope() {
        @Override
        public <T> T get(Contextual<T> contextual, CreationalContext<T> creationalContext) {
            throw new IllegalStateException("Scope is not active");
        }

        @Override
        public <T> T get(Contextual<T> contextual) {
            throw new IllegalStateException("Scope is not active");
        }

        @Override
        public void destroy() {

        }
    };

    private volatile Map<Contextual<?>, Instance<?>> instances = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public <T> T get(Contextual<T> contextual, CreationalContext<T> creationalContext) {
        Map<Contextual<?>, Instance<?>> instances = this.instances;
        if (instances == null) {
            return null;
        }

        return (T) instances.computeIfAbsent(contextual, c -> new Instance<>(contextual, creationalContext)).get();
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Contextual<T> contextual) {
        Map<Contextual<?>, Instance<?>> instances = this.instances;
        if (instances == null) {
            return null;
        }

        return (T) Optional.ofNullable(instances.get(contextual))
                .map(Instance::get)
                .orElse(null);
    }

    public void destroy() {
        Map<Contextual<?>, Instance<?>> instances = this.instances;
        this.instances = null;

        if (instances != null) {
            instances.values().forEach(Instance::destroy);
            instances.clear();
        }
    }

    private static class Instance<T> {
        private final CreationalContext<T> creationalContext;
        private final Contextual<T> contextual;
        private final T instance;

        Instance(Contextual<T> contextual, CreationalContext<T> creationalContext) {
            this.instance = contextual.create(creationalContext);
            this.creationalContext = creationalContext;
            this.contextual = contextual;
        }

        T get() {
            return instance;
        }

        void destroy() {
            contextual.destroy(instance, creationalContext);
        }
    }
}

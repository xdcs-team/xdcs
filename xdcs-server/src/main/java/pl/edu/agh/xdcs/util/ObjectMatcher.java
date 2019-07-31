package pl.edu.agh.xdcs.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author Kamil Jarosz
 */
public class ObjectMatcher<R> {
    private final Map<Class<?>, Function<?, ?>> handlers;
    private final Function<Object, R> otherHandler;

    private ObjectMatcher(Map<Class<?>, Function<?, ?>> handlers, Function<Object, R> otherHandler) {
        this.handlers = handlers;
        this.otherHandler = otherHandler;
    }

    public static <R> ObjectMatcherBuilder<R> newMatcher() {
        return new ObjectMatcherBuilder<>();
    }

    public R match(Object o) {
        return match0(o.getClass(), o);
    }

    @SuppressWarnings("unchecked")
    private <A> R match0(Class<?> clazz, Object o) {
        if (handlers.containsKey(clazz)) {
            Function<A, R> handler = (Function<A, R>) handlers.get(o.getClass());
            return handler.apply((A) o);
        }

        if (clazz == Object.class) {
            return otherHandler.apply(o);
        }

        return match0(clazz.getSuperclass(), o);
    }

    public static class ObjectMatcherBuilder<R> {
        private Map<Class<?>, Function<?, ?>> handlers = new HashMap<>();
        private Function<Object, R> otherHandler = o -> {
            throw new RuntimeException("No matcher for " + o);
        };

        public <A> ObjectMatcherBuilder<R> match(Class<A> clazz, Function<A, R> handler) {
            Objects.requireNonNull(clazz);
            Objects.requireNonNull(handler);
            handlers.put(clazz, handler);
            return this;
        }

        public ObjectMatcherBuilder<R> other(Function<Object, R> otherHandler) {
            Objects.requireNonNull(otherHandler);
            this.otherHandler = otherHandler;
            return this;
        }

        public ObjectMatcher<R> build() {
            return new ObjectMatcher<>(handlers, otherHandler);
        }
    }
}

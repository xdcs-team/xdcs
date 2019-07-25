package pl.edu.agh.xdcs.grpc.context;

import java.lang.annotation.Annotation;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * @author Kamil Jarosz
 */
public class StackableKeyedContext<K> extends KeyedContext<K> {
    private final ThreadLocal<ConcurrentLinkedDeque<K>> keyStack =
            ThreadLocal.withInitial(ConcurrentLinkedDeque::new);
    private final boolean automaticEviction;

    public StackableKeyedContext(Class<? extends Annotation> scopeAnnotation, boolean automaticEviction) {
        super(scopeAnnotation);
        this.automaticEviction = automaticEviction;
    }

    public K enter(K key) {
        K former = super.enter(key);
        if (former != null) {
            keyStack.get().addLast(former);
        }
        return former;
    }

    public K exit() {
        if (automaticEviction) {
            return exitWithEviction();
        } else {
            return exit0();
        }
    }

    public K exitWithEviction() {
        K key = exit0();
        evict(key);
        return key;
    }

    private K exit0() {
        return super.enter(keyStack.get().pollLast());
    }
}

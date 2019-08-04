package pl.edu.agh.xdcs.util;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;

/**
 * A bean used to eagerly initialize all beans annotated with {@link Eager}.
 *
 * @author Kamil Jarosz
 */
@ApplicationScoped
public class EagerInitiator {
    @Inject
    private Event<ApplicationStartedEvent> event;

    public void awake(@Observes @Initialized(ApplicationScoped.class) Object initiator) {

    }

    @PostConstruct
    public void init() {
        for (Object o : CDI.current().select(Object.class, Eager.INSTANCE)) {
            // force bean initialization
            o.toString();
        }

        event.fire(new ApplicationStartedEvent());
    }
}

package pl.edu.agh.xdcs.services.sweeper;

import com.google.common.base.Strings;
import org.slf4j.Logger;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

/**
 * @author Kamil Jarosz
 */
@SweepAfter
@Interceptor
@Priority(Interceptor.Priority.PLATFORM_BEFORE + 100)
public class SweepAfterInterceptor {
    @Inject
    private Logger logger;

    @Inject
    private TaskSweeper taskSweeper;

    @AroundInvoke
    public Object intercept(InvocationContext invocationContext) throws Exception {
        try {
            return invocationContext.proceed();
        } finally {
            String message = invocationContext.getMethod().getAnnotation(SweepAfter.class).message();
            logger.debug("Sweep initiated by interceptor" +
                    (!Strings.isNullOrEmpty(message) ? ": " + message : ""));
            taskSweeper.startSweeping();
        }
    }
}

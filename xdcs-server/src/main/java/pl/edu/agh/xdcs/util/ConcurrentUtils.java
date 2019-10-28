package pl.edu.agh.xdcs.util;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Kamil Jarosz
 */
public class ConcurrentUtils {
    private static ExecutorService applicationExecutorService;

    static {
        try {
            applicationExecutorService = InitialContext.doLookup("java:comp/env/concurrent/ThreadPool");
        } catch (NamingException e) {
            applicationExecutorService = Executors.newCachedThreadPool();
        }
    }

    public static ExecutorService applicationExecutorService() {
        return applicationExecutorService;
    }
}

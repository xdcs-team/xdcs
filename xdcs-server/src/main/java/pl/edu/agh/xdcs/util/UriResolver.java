package pl.edu.agh.xdcs.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.UriBuilder;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Kamil Jarosz
 */
public class UriResolver {
    private static final Logger logger = LoggerFactory.getLogger(UriResolver.class);

    private static final Pattern CLASS_NAME_PATTERN = Pattern.compile(
            "class " + Pattern.quote(Absurd.class.getName()) + " cannot be cast to class (?<classname>[^ ]+).*");

    static final String CONTEXT_ROOT = "/xdcs";

    @Inject
    private Instance<Application> applications;

    private final LoadingCache<MethodReferenceIdentifier, UriBuilder> methodUriTemplateCache = CacheBuilder.newBuilder()
            .build(new CacheLoader<MethodReferenceIdentifier, UriBuilder>() {
                @Override
                public UriBuilder load(MethodReferenceIdentifier id) {
                    return resolve(id.getF());
                }
            });

    @SuppressWarnings("unchecked")
    private <T> UriBuilder resolve(Function<T, ?> f) {
        Class<?> requiredInterface;
        try {
            f.apply((T) Absurd.INSTANCE);
            logger.error("Expected to catch a ClassCastException");
            throw new AssertionError();
        } catch (ClassCastException e) {
            Matcher matcher = CLASS_NAME_PATTERN.matcher(e.getMessage());
            if (!matcher.matches()) {
                logger.error("Cannot read class name from ClassCastException, " +
                        "" + e.getMessage() + " doesn't match " + CLASS_NAME_PATTERN, e);
                throw e;
            }

            String requiredClassName = matcher.group("classname");
            try {
                requiredInterface = Class.forName(requiredClassName);
            } catch (ClassNotFoundException notFoundException) {
                logger.error("Required class not found: " + requiredClassName, notFoundException);
                throw new UriResolverException(notFoundException);
            }
        }

        if (!requiredInterface.isInterface()) {
            throw new UriResolverException("Class " + requiredInterface + " is not an interface; " +
                    "an interface is required to resolve its method's URI");
        }

        Object proxy = Proxy.newProxyInstance(
                UriResolver.class.getClassLoader(),
                new Class[]{requiredInterface}, (p, method, args) -> {
                    throw new MethodInfoCarrier(method);
                });

        Method method;
        try {
            f.apply((T) proxy);
            logger.error("Expected to catch a MethodInfoCarrier");
            throw new AssertionError();
        } catch (MethodInfoCarrier e) {
            method = e.getMethod();
        }

        return resolve(method);
    }

    private <A> String resolveFromCache(Object key, Function<A, ?> f, Object... args) {
        try {
            UriBuilder builder = methodUriTemplateCache.get(MethodReferenceIdentifier.builder()
                    .f(f)
                    .key(key)
                    .build());
            return builder.build(args).toString();
        } catch (ExecutionException e) {
            throw new UriResolverException(e);
        }
    }

    private UriBuilder resolve(Method method) {
        return UriBuilder.fromPath("/")
                .path(CONTEXT_ROOT)
                .path(getApplicationPath(method.getDeclaringClass()))
                .path(method.getDeclaringClass())
                .path(method);
    }

    private String getApplicationPath(Class<?> resourceClass) {
        Class<?> applicationClass = null;
        int matchedLength = 0;
        for (Application app : applications) {
            String rPackage = resourceClass.getPackage().getName();
            String aPackage = app.getClass().getPackage().getName();

            int len = StringUtils.getCommonPrefix(aPackage, rPackage).length();
            if (len > matchedLength) {
                applicationClass = app.getClass();
                matchedLength = len;
            }
        }

        if (applicationClass == null) {
            throw new UriResolverException("Application not found for " + resourceClass);
        }

        while (applicationClass.getAnnotation(ApplicationPath.class) == null) {
            applicationClass = applicationClass.getSuperclass();
        }

        return applicationClass.getAnnotation(ApplicationPath.class).value();
    }

    public <A, R> String of(Functions.Function1<A, R> f, Object... args) {
        return this.resolveFromCache(f, f::apply, args);
    }

    public <A, B, R> String of(Functions.Function2<A, B, R> f, Object... args) {
        return this.<A>resolveFromCache(f, thiz -> f.apply(thiz, null), args);
    }

    public <A, B, C, R> String of(Functions.Function3<A, B, C, R> f, Object... args) {
        return this.<A>resolveFromCache(f, thiz -> f.apply(thiz, null, null), args);
    }

    public <A, B, C, D, R> String of(Functions.Function4<A, B, C, D, R> f, Object... args) {
        return this.<A>resolveFromCache(f, thiz -> f.apply(thiz, null, null, null), args);
    }

    public <A, B, C, D, E, R> String of(Functions.Function5<A, B, C, D, E, R> f, Object... args) {
        return this.<A>resolveFromCache(f, thiz -> f.apply(thiz, null, null, null, null), args);
    }

    public <A, B, C, D, E, F, R> String of(Functions.Function6<A, B, C, D, E, F, R> f, Object... args) {
        return this.<A>resolveFromCache(f, thiz -> f.apply(thiz, null, null, null, null, null), args);
    }

    private static final class Absurd {
        private static final Absurd INSTANCE = new Absurd();
    }

    private static final class MethodInfoCarrier extends Error {
        private final Method method;

        private MethodInfoCarrier(Method method) {
            this.method = method;
        }

        private Method getMethod() {
            return method;
        }
    }

    @Getter
    @Builder
    @EqualsAndHashCode(exclude = "f")
    private static final class MethodReferenceIdentifier {
        private Object key;
        private Function<?, ?> f;
    }
}

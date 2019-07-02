package pl.edu.agh.xdcs.restapi.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.xdcs.RestApplication;

import javax.ws.rs.ApplicationPath;
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

    private static final String CONTEXT_ROOT = "/xdcs";
    private static final String APPLICATION_PATH = RestApplication.class.getAnnotation(ApplicationPath.class).value();
    private static final Pattern CLASS_NAME_PATTERN = Pattern.compile(
            "class " + Pattern.quote(Absurd.class.getName()) + " cannot be cast to class (?<classname>[^ ]+).*");

    private static final LoadingCache<MethodReferenceIdentifier, UriBuilder> methodUriTemplateCache = CacheBuilder.newBuilder()
            .build(new CacheLoader<MethodReferenceIdentifier, UriBuilder>() {
                @Override
                public UriBuilder load(MethodReferenceIdentifier id) {
                    return resolve(id.getF());
                }
            });

    @SuppressWarnings("unchecked")
    private static <T> UriBuilder resolve(Function<T, ?> f) {
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
                throw new RuntimeException(notFoundException);
            }
        }

        if (!requiredInterface.isInterface()) {
            throw new RuntimeException("Class " + requiredInterface + " is not an interface; " +
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

    private static <A> String resolveFromCache(Object key, Function<A, ?> f, Object... args) {
        try {
            UriBuilder builder = methodUriTemplateCache.get(MethodReferenceIdentifier.builder()
                    .f(f)
                    .key(key)
                    .build());
            return builder.build(args).toString();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private static UriBuilder resolve(Method method) {
        return UriBuilder.fromPath("/")
                .path(CONTEXT_ROOT)
                .path(APPLICATION_PATH)
                .path(method.getDeclaringClass())
                .path(method);
    }

    public <A, R> String of(Functions.Function1<A, R> f, Object... args) {
        return UriResolver.resolveFromCache(f, f::apply, args);
    }

    public <A, B, R> String of(Functions.Function2<A, B, R> f, Object... args) {
        return UriResolver.<A>resolveFromCache(f, thiz -> f.apply(thiz, null), args);
    }

    public <A, B, C, R> String of(Functions.Function3<A, B, C, R> f, Object... args) {
        return UriResolver.<A>resolveFromCache(f, thiz -> f.apply(thiz, null, null), args);
    }

    public <A, B, C, D, R> String of(Functions.Function4<A, B, C, D, R> f, Object... args) {
        return UriResolver.<A>resolveFromCache(f, thiz -> f.apply(thiz, null, null, null), args);
    }

    public <A, B, C, D, E, R> String of(Functions.Function5<A, B, C, D, E, R> f, Object... args) {
        return UriResolver.<A>resolveFromCache(f, thiz -> f.apply(thiz, null, null, null, null), args);
    }

    private static final class Absurd {
        private static final Absurd INSTANCE = new Absurd();
    }

    private static final class MethodInfoCarrier extends Error {
        private Method method;

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

package pl.edu.agh.xdcs.grpc;

import javax.inject.Qualifier;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Kamil Jarosz
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface Service {
    Service INSTANCE = new Service() {
        @Override
        public Class<? extends Annotation> annotationType() {
            return Service.class;
        }
    };
}

package pl.edu.agh.xdcs.util;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Qualifier;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A qualifier indicating that the annotated bean shall be initialized eagerly,
 * i.e. when the application starts.
 * <p>
 * This qualifier should be used only on {@link ApplicationScoped} beans.
 *
 * @author Kamil Jarosz
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Eager {
    Eager INSTANCE = new Eager() {
        @Override
        public Class<? extends Annotation> annotationType() {
            return Eager.class;
        }
    };
}

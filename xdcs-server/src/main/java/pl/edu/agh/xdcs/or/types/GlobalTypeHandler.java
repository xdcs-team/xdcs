package pl.edu.agh.xdcs.or.types;

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
@Target(ElementType.TYPE)
public @interface GlobalTypeHandler {
    class Literal {
        public static GlobalTypeHandler instance() {
            return new GlobalTypeHandler() {
                @Override
                public Class<? extends Annotation> annotationType() {
                    return GlobalTypeHandler.class;
                }
            };
        }
    }
}

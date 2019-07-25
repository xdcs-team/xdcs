package pl.edu.agh.xdcs.grpc.scope;

import javax.enterprise.context.NormalScope;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies that a bean is agent scoped.
 * <p>
 * Beans will survive session teardown.
 * <p>
 * The agent context is destroyed when the application is shut down.
 *
 * @author Kamil Jarosz
 */
@Documented
@NormalScope
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
public @interface AgentScoped {

}

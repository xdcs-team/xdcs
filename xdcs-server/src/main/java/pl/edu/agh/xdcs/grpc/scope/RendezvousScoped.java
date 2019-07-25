package pl.edu.agh.xdcs.grpc.scope;

import javax.enterprise.context.NormalScope;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies that a bean is GRPC rendezvous scoped.
 * <p>
 * A rendezvous is equivalent to a GRPC call, it starts upon GRPC requests and
 * ends along with call close / cancel.
 *
 * @author Kamil Jarosz
 */
@Documented
@NormalScope
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
public @interface RendezvousScoped {

}

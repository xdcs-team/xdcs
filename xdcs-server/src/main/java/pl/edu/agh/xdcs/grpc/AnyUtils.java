package pl.edu.agh.xdcs.grpc;

import com.google.protobuf.Any;

import java.util.Optional;

/**
 * @author Kamil Jarosz
 */
class AnyUtils {
    private static final String GRPC_PACKAGE = "xdcs.agent";
    private static final String JAVA_PACKAGE = "pl.edu.agh.xdcs.api";

    static Optional<Class<?>> typeOf(Any any) {
        String typeUrl = any.getTypeUrl();
        String expectedPrefix = "type.googleapis.com/" + GRPC_PACKAGE + ".";
        if (!typeUrl.startsWith(expectedPrefix)) {
            return Optional.empty();
        }

        String typeName = typeUrl.substring(expectedPrefix.length());
        if (typeName.contains(".")) {
            return Optional.empty();
        }

        String className = JAVA_PACKAGE + "." + typeName;
        try {
            return Optional.of(Class.forName(className));
        } catch (ClassNotFoundException e) {
            return Optional.empty();
        }
    }
}

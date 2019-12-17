package pl.edu.agh.xdcs.restapi.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;

/**
 * @author Kamil Jarosz
 */
public class RestUtils {
    private static final Logger logger = LoggerFactory.getLogger(RestUtils.class);

    public static Response created(String resourceLocation) {
        return Response.created(URI.create(resourceLocation)).build();
    }

    public static Response badRequest(String reason) {
        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(RestErrorResponse.builder()
                        .error(reason)
                        .build())
                .build();
    }

    public static BadRequestException throwBadRequest(String reason) {
        throw new BadRequestException(badRequest(reason));
    }

    public static Response unprocessableEntity(String reason) {
        return Response.status(422)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(RestErrorResponse.builder()
                        .error(reason)
                        .build())
                .build();
    }

    public static Response serverError(String reason) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(RestErrorResponse.builder()
                        .error(reason)
                        .build())
                .build();
    }

    public static Response serverError(Throwable cause) {
        logger.error("Server error occurred", cause);
        RestErrorResponse entity = RestErrorResponse.builder()
                .error(cause.getClass().getName() + ": " + cause.getMessage())
                .build();

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(entity)
                .build();
    }

    public static void checkNotNull(Object object, String message) {
        if (object == null) {
            throw new BadRequestException(message, badRequest(message));
        }
    }

    public static void check(boolean condition, String message) {
        if (!condition) {
            throw new BadRequestException(message, badRequest(message));
        }
    }
}

package pl.edu.agh.xdcs.util;

import javax.websocket.server.ServerEndpoint;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author Kamil Jarosz
 */
public class WsUriResolver {
    public URI of(Class<?> wsClass, UriInfo uriInfo, Object... args) {
        URI baseUri = uriInfo.getBaseUri();
        URI wsBaseUri;
        try {
            wsBaseUri = new URI(
                    getScheme(uriInfo),
                    baseUri.getUserInfo(),
                    baseUri.getHost(),
                    baseUri.getPort(),
                    null, null, null);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        String wsPath = wsClass.getAnnotation(ServerEndpoint.class).value();
        return UriBuilder.fromUri(wsBaseUri)
                .path(UriResolver.CONTEXT_ROOT)
                .path(wsPath)
                .build(args);
    }

    private String getScheme(UriInfo uriInfo) {
        return uriInfo.getBaseUri().getScheme().equals("https") ? "wss" : "ws";
    }
}

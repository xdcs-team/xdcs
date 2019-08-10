package pl.edu.agh.xdcs.security.web;

import pl.edu.agh.xdcs.RestApplication;
import pl.edu.agh.xdcs.security.Token;

import javax.inject.Inject;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.net.URI;
import java.util.Optional;

/**
 * @author Kamil Jarosz
 */
@Provider
public class SecurityFilter implements ContainerRequestFilter {
    private static final String AUTH_PREFIX = RestApplication.CONTEXT_ROOT + AuthApplication.class.getAnnotation(ApplicationPath.class).value();
    private static final String SCHEME = "Bearer ";

    private static final Response ACCESS_DENIED = Response.status(Response.Status.UNAUTHORIZED).build();

    @Inject
    private AuthenticationService authenticationService;

    @Inject
    private UserContext userContext;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        URI requestUri = requestContext.getUriInfo().getRequestUri();
        if (requestUri.getPath().startsWith(AUTH_PREFIX)) {
            // auth endpoints are not secured
            return;
        }

        String authorization = requestContext.getHeaderString("Authorization");
        if (authorization == null || !authorization.startsWith(SCHEME)) {
            requestContext.abortWith(ACCESS_DENIED);
            return;
        }

        String token = authorization.substring(SCHEME.length());
        if (token.isEmpty()) {
            requestContext.abortWith(ACCESS_DENIED);
            return;
        }

        Optional<String> username = authenticationService.validateToken(token)
                .map(Token::getSubject);
        if (!username.isPresent()) {
            requestContext.abortWith(ACCESS_DENIED);
            return;
        }

        userContext.setUsername(username.get());
    }
}

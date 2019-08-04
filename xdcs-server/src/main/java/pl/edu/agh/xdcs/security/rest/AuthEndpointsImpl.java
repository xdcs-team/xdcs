package pl.edu.agh.xdcs.security.rest;

import com.google.common.io.ByteStreams;
import pl.edu.agh.xdcs.security.AuthenticationService;
import pl.edu.agh.xdcs.util.UriResolver;

import javax.inject.Inject;
import javax.security.enterprise.AuthenticationException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * @author Kamil Jarosz
 */
public class AuthEndpointsImpl implements AuthEndpoints {
    @Context
    private HttpServletRequest request;

    @Context
    private HttpServletResponse response;

    @Inject
    private AuthenticationService authenticationService;

    @Context
    private UriInfo uriInfo;

    @Inject
    private UriResolver resolver;

    private Response validateParameters(String responseType, String clientId, String redirectUri) {
        if (!"code".equals(responseType)) {
            return badRequest("Invalid response_type");
        }

        if (!"web".equals(clientId)) {
            return badRequest("Invalid client_id");
        }

        if (redirectUri != null &&
                !redirectUri.startsWith(uriInfo.getBaseUri().toString()) &&
                !redirectUri.startsWith("/")) {
            return badRequest("Invalid redirect_uri");
        }

        return null;
    }

    private Response badRequest(String s) {
        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(ErrorEntity.builder()
                        .error(s)
                        .build())
                .build();
    }

    @Override
    public Response authorize(String responseType, String clientId, String redirectUri) {
        Response errorResponse = validateParameters(responseType, clientId, redirectUri);
        if (errorResponse != null) return errorResponse;

        try {
            return renderSignIn(UriBuilder.fromUri(resolver.of(AuthEndpoints::doAuthorize))
                    .queryParam("response_type", responseType)
                    .queryParam("client_id", clientId)
                    .queryParam("redirect_uri", redirectUri)
                    .build());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Response renderSignIn(URI callback) throws IOException {
        InputStream index = AuthEndpointsImpl.class.getClassLoader()
                .getResourceAsStream("auth/sign-in/index.html");
        String page = new String(ByteStreams.toByteArray(index), StandardCharsets.UTF_8);
        page = page.replaceAll("\\{\\{callback}}", callback.toString());
        return Response.ok(page).build();
    }

    @Override
    public Response doAuthorize(String responseType, String clientId, String redirectUri, String username, String password) {
        try {
            Response errorResponse = validateParameters(responseType, clientId, redirectUri);
            if (errorResponse != null) return errorResponse;
            String code = authenticationService.authenticate(username, password);

            if (redirectUri != null) {
                return Response.status(Response.Status.FOUND)
                        .header("Location", UriBuilder.fromUri(redirectUri)
                                .queryParam("code", code)
                                .build()
                                .toString()).build();
            } else {
                return Response.ok(CodeGrant.builder()
                        .code(code)
                        .build()).build();
            }
        } catch (AuthenticationException e) {
            return badRequest("Authorization failed: " + e.getMessage());
        }
    }

    @Override
    public Response getToken(String grantType, String code, String refreshToken) {
        int accessTokenExpirationSeconds = (int) (authenticationService.getAccessTokenExpirationTime().toMillis() / 1000);

        try {
            if ("authorization_code".equals(grantType)) {
                String newRefreshToken = authenticationService.generateRefreshToken(code);
                String newAccessToken = authenticationService.generateAccessToken(newRefreshToken);
                TokenGrant grant = TokenGrant.builder()
                        .tokenType("bearer")
                        .expiresIn(accessTokenExpirationSeconds)
                        .refreshToken(newRefreshToken)
                        .accessToken(newAccessToken)
                        .build();
                return Response.ok().entity(grant).build();
            } else if ("refresh_token".equals(grantType)) {
                String newAccessToken = authenticationService.generateAccessToken(refreshToken);
                TokenGrant grant = TokenGrant.builder()
                        .tokenType("bearer")
                        .expiresIn(accessTokenExpirationSeconds)
                        .refreshToken(refreshToken)
                        .accessToken(newAccessToken)
                        .build();
                return Response.ok().entity(grant).build();
            } else {
                return badRequest("Unknown grant_type");
            }
        } catch (AuthenticationException e) {
            return badRequest("Authorization failed: " + e.getMessage());
        }
    }
}

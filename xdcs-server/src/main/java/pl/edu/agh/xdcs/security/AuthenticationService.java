package pl.edu.agh.xdcs.security;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import pl.edu.agh.xdcs.config.Configured;
import pl.edu.agh.xdcs.config.WebExpirationTimes;
import pl.edu.agh.xdcs.config.WebSecurityConfiguration;
import pl.edu.agh.xdcs.util.ApplicationStartedEvent;
import pl.edu.agh.xdcs.util.Enabled;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.security.enterprise.AuthenticationException;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Kamil Jarosz
 */
@ApplicationScoped
public class AuthenticationService {
    private Cache<String, String> authCodes;
    private Cache<String, String> refreshTokens;
    private Cache<String, String> accessTokens;

    @Inject
    @Enabled
    private Instance<WebAuthenticator> authenticators;

    @Inject
    @Configured
    private WebSecurityConfiguration webSecurityConfiguration;

    private void wake(@Observes ApplicationStartedEvent event) {

    }

    public Duration getAuthCodeExpirationTime() {
        return Optional.ofNullable(webSecurityConfiguration.getExpirationTimes())
                .map(WebExpirationTimes::getAuthCode)
                .orElse(Duration.ofSeconds(30));
    }

    public Duration getRefreshTokenExpirationTime() {
        return Optional.ofNullable(webSecurityConfiguration.getExpirationTimes())
                .map(WebExpirationTimes::getRefreshToken)
                .orElse(Duration.ofDays(7));
    }

    public Duration getAccessTokenExpirationTime() {
        return Optional.ofNullable(webSecurityConfiguration.getExpirationTimes())
                .map(WebExpirationTimes::getAccessToken)
                .orElse(Duration.ofMinutes(10));
    }

    @PostConstruct
    private void initializeCaches() {
        authCodes = CacheBuilder.newBuilder()
                .expireAfterWrite(getAuthCodeExpirationTime())
                .maximumSize(1024)
                .build();

        refreshTokens = CacheBuilder.newBuilder()
                .expireAfterAccess(getRefreshTokenExpirationTime())
                .maximumSize(1024)
                .build();

        accessTokens = CacheBuilder.newBuilder()
                .expireAfterWrite(getAccessTokenExpirationTime())
                .maximumSize(1024)
                .build();
    }

    private void stall() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public String authenticate(String username, String password) throws AuthenticationException {
        for (WebAuthenticator authenticator : authenticators) {
            if (authenticator.authenticate(username, password)) {
                String code = UUID.randomUUID().toString();
                authCodes.put(code, username);
                return code;
            }
        }

        stall();
        throw new AuthenticationException("Invalid username or password");
    }

    public String generateRefreshToken(String code) throws AuthenticationException {
        String username = authCodes.getIfPresent(code);
        if (username == null) {
            stall();
            throw new AuthenticationException("Invalid code");
        }

        String token = UUID.randomUUID().toString();
        refreshTokens.put(token, username);
        return token;
    }

    public String generateAccessToken(String refreshToken) throws AuthenticationException {
        String username = refreshTokens.getIfPresent(refreshToken);
        if (username == null) {
            stall();
            throw new AuthenticationException("Invalid refresh token");
        }

        String token = UUID.randomUUID().toString();
        accessTokens.put(token, username);
        return token;
    }

    public Optional<String> getUsername(String accessToken) {
        return Optional.ofNullable(accessTokens.getIfPresent(accessToken));
    }
}

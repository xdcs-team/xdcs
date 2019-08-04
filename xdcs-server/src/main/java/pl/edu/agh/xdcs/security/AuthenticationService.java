package pl.edu.agh.xdcs.security;

import pl.edu.agh.xdcs.config.Configured;
import pl.edu.agh.xdcs.config.WebExpirationTimes;
import pl.edu.agh.xdcs.config.WebSecurityConfiguration;
import pl.edu.agh.xdcs.util.ApplicationStartedEvent;
import pl.edu.agh.xdcs.util.Enabled;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.security.enterprise.AuthenticationException;
import java.time.Duration;
import java.util.Optional;

/**
 * @author Kamil Jarosz
 */
@ApplicationScoped
public class AuthenticationService {
    @Inject
    @Enabled
    private Instance<WebAuthenticator> authenticators;

    @Inject
    private TokenIssuer issuer;

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
                return issuer.issueToken(username, getRefreshTokenExpirationTime(), TokenIssuer.TokenType.AUTH_CODE);
            }
        }

        stall();
        throw new AuthenticationException("Invalid username or password");
    }

    public String generateRefreshToken(String code) throws AuthenticationException {
        String username = issuer.validateToken(code, TokenIssuer.TokenType.AUTH_CODE)
                .map(Token::getUsername)
                .orElse(null);
        if (username == null) {
            stall();
            throw new AuthenticationException("Invalid code");
        }

        return issuer.issueToken(username, getRefreshTokenExpirationTime(), TokenIssuer.TokenType.REFRESH);
    }

    public String generateAccessToken(String refreshToken) throws AuthenticationException {
        String username = issuer.validateToken(refreshToken, TokenIssuer.TokenType.REFRESH)
                .map(Token::getUsername)
                .orElse(null);
        if (username == null) {
            stall();
            throw new AuthenticationException("Invalid refresh token");
        }

        return issuer.issueToken(username, getAccessTokenExpirationTime(), TokenIssuer.TokenType.ACCESS);
    }

    public Optional<Token> validateToken(String accessToken) {
        return issuer.validateToken(accessToken, TokenIssuer.TokenType.ACCESS);
    }
}

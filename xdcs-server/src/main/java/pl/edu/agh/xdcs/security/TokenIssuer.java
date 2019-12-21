package pl.edu.agh.xdcs.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import pl.edu.agh.xdcs.config.AgentSecurityConfiguration;
import pl.edu.agh.xdcs.config.Configured;
import pl.edu.agh.xdcs.config.KeyPathConfiguration;
import pl.edu.agh.xdcs.config.WebSecurityConfiguration;
import pl.edu.agh.xdcs.config.util.ReferencedFileLoader;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Key;
import java.sql.Date;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * @author Kamil Jarosz
 */
@ApplicationScoped
public class TokenIssuer {
    private Key webKey;
    private Key agentKey;

    @Inject
    @Configured
    private WebSecurityConfiguration webSecurityConfiguration;

    @Inject
    @Configured
    private AgentSecurityConfiguration agentSecurityConfiguration;

    @Inject
    private ReferencedFileLoader fileLoader;

    @PostConstruct
    private void loadKeys() {
        try {
            webKey = loadOrCreateKey(webSecurityConfiguration.getJwtKey());
            agentKey = loadOrCreateKey(agentSecurityConfiguration.getJwtKey());
        } catch (IOException e) {
            throw new UncheckedIOException("IO error while reading JWT keys", e);
        }
    }

    private Key loadOrCreateKey(KeyPathConfiguration config) throws IOException {
        Path path = Optional.ofNullable(config)
                .map(KeyPathConfiguration::getPath)
                .map(fileLoader::toPath)
                .orElse(null);

        if (path == null) {
            return Keys.secretKeyFor(SignatureAlgorithm.HS512);
        }

        if (Files.isReadable(path)) {
            byte[] encoded = Files.readAllBytes(path);
            return Keys.hmacShaKeyFor(encoded);
        }

        if (Files.exists(path)) {
            throw new IOException("File " + path + " exists but is not a regular file");
        }

        Key key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
        byte[] encoded = key.getEncoded();
        Files.write(path, encoded);
        return key;
    }

    private Key getKey(TokenType type) {
        return type == TokenType.AGENT ? agentKey : webKey;
    }

    public String issueToken(String user, Duration expirationTime, TokenType type) {
        return issueToken(user, expirationTime, type, Collections.emptyMap());
    }

    public String issueToken(String user, Duration expirationTime, TokenType type, Map<String, Object> claims) {
        java.util.Date expirationDate = expirationTime == null ? null :
                Date.from(Instant.now().plusMillis(expirationTime.toMillis()));

        return Jwts.builder().setSubject(user)
                .setExpiration(expirationDate)
                .claim("type", type.toString())
                .addClaims(claims)
                .signWith(getKey(type))
                .compact();
    }

    public Optional<Token> validateToken(String token, TokenType expectedType) {
        try {
            Claims claims = Jwts.parser().setSigningKey(getKey(expectedType))
                    .parseClaimsJws(token)
                    .getBody();

            if (!expectedType.toString()
                    .equals(claims.get("type", String.class))) {
                return Optional.empty();
            }

            return Optional.of(new Token(token, claims));
        } catch (JwtException e) {
            return Optional.empty();
        }
    }

    public enum TokenType {
        AUTH_CODE("auth_code"),
        REFRESH("refresh"),
        ACCESS("access"),
        AGENT("agent"),
        ;

        private String value;

        TokenType(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }
}

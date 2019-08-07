package pl.edu.agh.xdcs.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.enterprise.context.ApplicationScoped;
import java.security.Key;
import java.sql.Date;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * @author Kamil Jarosz
 */
@ApplicationScoped
public class TokenIssuer {
    private Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    public String issueToken(String user, Duration expirationTime, TokenType type) {
        Instant expirationDate = Instant.now().plusMillis(expirationTime.toMillis());
        return Jwts.builder().setSubject(user)
                .setExpiration(Date.from(expirationDate))
                .claim("type", type.toString())
                .signWith(key)
                .compact();
    }

    public Optional<Token> validateToken(String token, TokenType expectedType) {
        try {
            Claims claims = Jwts.parser().setSigningKey(key)
                    .parseClaimsJws(token)
                    .getBody();

            if (!expectedType.toString()
                    .equals(claims.get("type", String.class))) {
                return Optional.empty();
            }

            return Optional.of(new Token(claims));
        } catch (JwtException e) {
            return Optional.empty();
        }
    }

    public enum TokenType {
        AUTH_CODE("auth_code"),
        REFRESH("refresh"),
        ACCESS("access"),
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

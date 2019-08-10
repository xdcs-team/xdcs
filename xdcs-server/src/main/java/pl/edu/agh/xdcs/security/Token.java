package pl.edu.agh.xdcs.security;

import io.jsonwebtoken.Claims;

/**
 * @author Kamil Jarosz
 */
public class Token {
    private final String token;
    private final Claims claims;

    Token(String token, Claims claims) {
        this.token = token;
        this.claims = claims;
    }

    public String getSubject() {
        return claims.getSubject();
    }

    @Override
    public String toString() {
        return token;
    }

    public <T> T getClaim(String name, Class<T> clazz) {
        return claims.get(name, clazz);
    }
}

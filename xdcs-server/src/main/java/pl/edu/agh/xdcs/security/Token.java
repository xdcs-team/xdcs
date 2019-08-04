package pl.edu.agh.xdcs.security;

import io.jsonwebtoken.Claims;

/**
 * @author Kamil Jarosz
 */
public class Token {
    private final Claims claims;

    Token(Claims claims) {
        this.claims = claims;
    }

    public String getUsername() {
        return claims.getSubject();
    }
}

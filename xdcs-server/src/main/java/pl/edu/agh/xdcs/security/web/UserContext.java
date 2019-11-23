package pl.edu.agh.xdcs.security.web;

import lombok.Getter;
import lombok.Setter;

import javax.enterprise.context.RequestScoped;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * @author Kamil Jarosz
 */
@RequestScoped
@Getter
@Setter
public class UserContext {
    private String username;
    private ZoneId zoneId = ZoneId.of("UTC");

    public ZoneOffset getCurrentZoneOffset() {
        return zoneId.getRules().getOffset(Instant.now());
    }
}

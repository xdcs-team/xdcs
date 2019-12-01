package pl.edu.agh.xdcs.security.web;

import lombok.Getter;
import lombok.Setter;

import javax.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * @author Kamil Jarosz
 */
@ApplicationScoped
public class UserContext {
    private ThreadLocal<UserData> userData = ThreadLocal.withInitial(UserData::new);

    private String getUsername() {
        return userData.get().getUsername();
    }

    public void setUsername(String username) {
        userData.get().setUsername(username);
    }

    public ZoneId getZoneId() {
        return userData.get().getZoneId();
    }

    public ZoneOffset getCurrentZoneOffset() {
        return getZoneId().getRules().getOffset(Instant.now());
    }

    public void clear() {
        userData.set(new UserData());
    }

    @Getter
    @Setter
    private static class UserData {
        private String username = null;
        private ZoneId zoneId = ZoneId.of("UTC");
    }
}

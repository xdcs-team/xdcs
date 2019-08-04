package pl.edu.agh.xdcs.security.authenticators;

import org.apache.commons.codec.digest.Crypt;
import pl.edu.agh.xdcs.security.WebAuthenticator;
import pl.edu.agh.xdcs.util.ShadowFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * @author Kamil Jarosz
 */
public class ShadowFileWebAuthenticator implements WebAuthenticator {
    private final Map<String, String> passwords;

    public ShadowFileWebAuthenticator(InputStream is) throws IOException {
        this.passwords = new ShadowFile(is).getPasswords();
    }

    @Override
    public boolean authenticate(String username, String password) {
        String pass = passwords.get(username);
        return pass != null && pass.equals(Crypt.crypt(password, pass));
    }
}

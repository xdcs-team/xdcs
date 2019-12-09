package pl.edu.agh.xdcs.ssh.authenticators;

import pl.edu.agh.xdcs.util.ShadowFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Kamil Jarosz
 */
public class ShadowFilePasswordAuthenticator extends StaticCryptPasswordAuthenticator {
    public ShadowFilePasswordAuthenticator(InputStream shadowFile) throws IOException {
        super(new ShadowFile(shadowFile).getPasswords());
    }
}

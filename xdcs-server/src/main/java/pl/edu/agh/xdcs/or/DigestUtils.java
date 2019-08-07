package pl.edu.agh.xdcs.or;

import com.google.common.io.BaseEncoding;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Kamil Jarosz
 */
class DigestUtils {
    static String digest(byte[] input) {
        try {
            return digest(new ByteArrayInputStream(input));
        } catch (IOException e) {
            throw new Error(e);
        }
    }

    static String digest(InputStream is) throws IOException {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new Error(e);
        }

        int read = 0;
        byte[] buffer = new byte[8 * 1024];
        while (read != -1) {
            read = is.read(buffer);
            if (read > 0) {
                digest.update(buffer, 0, read);
            }
        }
        return hexify(digest.digest());
    }

    private static String hexify(byte[] digest) {
        return BaseEncoding.base16().lowerCase().encode(digest);
    }
}

package pl.edu.agh.xdcs.util;

import com.google.common.base.Strings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Kamil Jarosz
 */
public class ShadowFile {
    private final Map<String, String> passwords;

    public ShadowFile(InputStream is) throws IOException {
        this.passwords = parseShadow(is);
    }

    static Map<String, String> parseShadow(InputStream shadowFile) throws IOException {
        Map<String, String> passwords = new HashMap<>();

        Reader r = new BufferedReader(new InputStreamReader(shadowFile, StandardCharsets.UTF_8));
        StreamTokenizer tokenizer = new StreamTokenizer(r);

        tokenizer.eolIsSignificant(true);
        tokenizer.wordChars(0, '\n' - 1);
        tokenizer.wordChars('\n' + 1, Character.MAX_CODE_POINT);
        tokenizer.whitespaceChars(':', ':');

        while (parseLine(passwords, tokenizer)) {
            // continue parsing
        }

        return passwords;
    }

    /**
     * @return {@code true} if a line has been parsed, {@code false otherwise}
     */
    private static boolean parseLine(Map<String, String> passwords, StreamTokenizer tokenizer) throws IOException {
        int next = tokenizer.nextToken();
        if (next == StreamTokenizer.TT_EOL) {
            // empty line
            return true;
        }

        if (next == StreamTokenizer.TT_EOF) {
            // end of file
            return false;
        }

        tokenizer.pushBack();
        String username = readValue(tokenizer);
        String password = readValue(tokenizer);
        if (!Strings.isNullOrEmpty(username) &&
                !Strings.isNullOrEmpty(password) &&
                !password.startsWith("!") &&
                !password.equals("*")) {
            passwords.put(username, password);
        }

        while (true) {
            int next2 = tokenizer.nextToken();
            if (next2 == StreamTokenizer.TT_EOL) {
                // end of line
                return true;
            }

            if (next2 == StreamTokenizer.TT_EOF) {
                // end of file
                return false;
            }
        }
    }

    private static String readValue(StreamTokenizer tokenizer) throws IOException {
        int next = tokenizer.nextToken();
        if (next != StreamTokenizer.TT_WORD) {
            tokenizer.pushBack();
            return null;
        }

        return tokenizer.sval;
    }

    public Map<String, String> getPasswords() {
        return passwords;
    }
}

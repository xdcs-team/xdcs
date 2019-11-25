package pl.edu.agh.xdcs.util;

import java.util.StringTokenizer;
import java.util.regex.Pattern;

/**
 * @author Kamil Jarosz
 */
public class WildcardPattern {
    private final Pattern pattern;
    private final String textPattern;

    private WildcardPattern(String pattern) {
        this.textPattern = pattern;
        this.pattern = convert(pattern);
    }

    public static WildcardPattern parse(String pattern) {
        return new WildcardPattern(pattern);
    }

    private static Pattern convert(String pattern) {
        StringTokenizer tokenizer = new StringTokenizer(pattern, "*?", true);
        StringBuilder finalPattern = new StringBuilder();
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if ("*".equals(token)) {
                finalPattern.append("(.*)");
            } else if ("?".equals(token)) {
                finalPattern.append("(.)");
            } else {
                finalPattern.append(Pattern.quote(token));
            }
        }
        return Pattern.compile(finalPattern.toString());
    }

    public boolean matches(String string) {
        return pattern.matcher(string).matches();
    }

    public Pattern toPattern() {
        return pattern;
    }

    @Override
    public String toString() {
        return textPattern;
    }
}

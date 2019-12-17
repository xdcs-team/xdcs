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
        this.pattern = toPattern(pattern);
    }

    public static WildcardPattern parse(String pattern) {
        return new WildcardPattern(pattern);
    }

    private static Pattern toPattern(String pattern) {
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

    public static WildcardPattern parseLike(String patternAgentNameLike) {
        throw null;
    }

    public boolean matches(String string) {
        return pattern.matcher(string).matches();
    }

    public Pattern toPattern() {
        return pattern;
    }

    public String toSqlLike() {
        StringTokenizer tokenizer = new StringTokenizer(textPattern, "*?", true);
        StringBuilder likePattern = new StringBuilder();
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if ("*".equals(token)) {
                likePattern.append("%");
            } else if ("?".equals(token)) {
                likePattern.append("_");
            } else {
                likePattern.append(token.replaceAll("%", "\\%")
                        .replaceAll("_", "\\_"));
            }
        }
        return likePattern.toString();
    }

    @Override
    public String toString() {
        return textPattern;
    }
}

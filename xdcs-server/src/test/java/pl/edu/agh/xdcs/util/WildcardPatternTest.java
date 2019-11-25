package pl.edu.agh.xdcs.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Kamil Jarosz
 */
class WildcardPatternTest {
    @Test
    void testConstantString() {
        WildcardPattern pattern = WildcardPattern.parse("asdf");
        assertThat(pattern.matches("asdf")).isTrue();
        assertThat(pattern.matches("asdf2")).isFalse();
        assertThat(pattern.matches("aasdf")).isFalse();
        assertThat(pattern.matches("")).isFalse();
    }

    @Test
    void testEmptyString() {
        WildcardPattern pattern = WildcardPattern.parse("");
        assertThat(pattern.matches("")).isTrue();
        assertThat(pattern.matches("asdf")).isFalse();
    }

    @Test
    void testQuestionMark() {
        WildcardPattern pattern = WildcardPattern.parse("a?s?d");
        assertThat(pattern.matches("asd")).isFalse();
        assertThat(pattern.matches("axsxd")).isTrue();
        assertThat(pattern.matches("axsyd")).isTrue();
    }

    @Test
    void testTrailingQuestionMark() {
        WildcardPattern pattern = WildcardPattern.parse("a?");
        assertThat(pattern.matches("asd")).isFalse();
        assertThat(pattern.matches("ab")).isTrue();
        assertThat(pattern.matches("a")).isFalse();
    }

    @Test
    void testAsterisk() {
        WildcardPattern pattern = WildcardPattern.parse("a*d");
        assertThat(pattern.matches("asd")).isTrue();
        assertThat(pattern.matches("axsxd")).isTrue();
        assertThat(pattern.matches("asdf")).isFalse();
    }

    @Test
    void testTrailingAsterisk() {
        WildcardPattern pattern = WildcardPattern.parse("a*");
        assertThat(pattern.matches("asd")).isTrue();
        assertThat(pattern.matches("a")).isTrue();
        assertThat(pattern.matches("ab")).isTrue();
        assertThat(pattern.matches("bab")).isFalse();
    }

    @Test
    void testAsteriskAndQuestionMarks() {
        WildcardPattern pattern = WildcardPattern.parse("a*d?");
        assertThat(pattern.matches("asd")).isFalse();
        assertThat(pattern.matches("axsxd")).isFalse();
        assertThat(pattern.matches("asdf")).isTrue();
    }
}

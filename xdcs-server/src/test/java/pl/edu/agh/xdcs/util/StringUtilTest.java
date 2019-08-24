package pl.edu.agh.xdcs.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Kamil Jarosz
 */
class StringUtilTest {
    @Test
    void emptyString() {
        assertThat(StringUtil.breakByLength("", 10))
                .isEmpty();
    }

    @Test
    void invalidLength() {
        assertThatThrownBy(() -> StringUtil.breakByLength("asdf", 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> StringUtil.breakByLength("asdf", -10))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void nullString() {
        assertThatThrownBy(() -> StringUtil.breakByLength(null, 2))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void unevenBreak() {
        assertThat(StringUtil.breakByLength("123412341234123", 4))
                .containsExactly("1234", "1234", "1234", "123");

        assertThat(StringUtil.breakByLength("12345", 4))
                .containsExactly("1234", "5");
    }

    @Test
    void evenBreak() {
        assertThat(StringUtil.breakByLength("12345", 1))
                .containsExactly("1", "2", "3", "4", "5");

        assertThat(StringUtil.breakByLength("121212", 2))
                .containsExactly("12", "12", "12");
    }

    @Test
    void oneLine() {
        assertThat(StringUtil.breakByLength("12345", 5))
                .containsExactly("12345");

        assertThat(StringUtil.breakByLength("12345", 20))
                .containsExactly("12345");
    }
}

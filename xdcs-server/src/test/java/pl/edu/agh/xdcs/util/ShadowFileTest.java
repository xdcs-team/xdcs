package pl.edu.agh.xdcs.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Kamil Jarosz
 */
class ShadowFileTest {
    private static Stream<Arguments> provideShadowFiles() {
        return Stream.of(
                Arguments.of("a:b:c:d:", new String[]{"a", "b"}),
                Arguments.of("a:b", new String[]{"a", "b"}),
                Arguments.of("a:b:", new String[]{"a", "b"}),
                Arguments.of("a:b:\nc:d:t\n", new String[]{"a", "b", "c", "d"}),
                Arguments.of("a:b:\nc:d:", new String[]{"a", "b", "c", "d"}),
                Arguments.of("", new String[]{}),
                Arguments.of(":b:\nc:d:", new String[]{"c", "d"}),
                Arguments.of("a:!b:", new String[]{}),
                Arguments.of("a:!", new String[]{}),
                Arguments.of("a:*", new String[]{}),
                Arguments.of("a:*ng\nb:p", new String[]{"a", "*ng", "b", "p"}),
                Arguments.of("a::", new String[]{}),
                Arguments.of("a::\nb:", new String[]{}),
                Arguments.of("a\nb\n\n", new String[]{})
        );
    }

    @ParameterizedTest
    @MethodSource("provideShadowFiles")
    void parseShadow(String shadowFile, String... expectedUsernamesAndPasswords) throws IOException {
        Map<String, String> expectedMap = new HashMap<>();
        for (int i = 0; i < expectedUsernamesAndPasswords.length; i += 2) {
            expectedMap.put(expectedUsernamesAndPasswords[i], expectedUsernamesAndPasswords[i + 1]);
        }

        ByteArrayInputStream is = new ByteArrayInputStream(shadowFile.getBytes(StandardCharsets.UTF_8));
        assertThat(ShadowFile.parseShadow(is))
                .isEqualTo(expectedMap);
    }
}

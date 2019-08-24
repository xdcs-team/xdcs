package pl.edu.agh.xdcs.util;

import com.google.common.base.Preconditions;
import com.google.common.collect.Streams;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

/**
 * @author Kamil Jarosz
 */
public class StringUtil {
    private StringUtil() {

    }

    public static Stream<String> breakByLength(String string, int length) {
        Preconditions.checkArgument(length > 0);
        if (string.isEmpty()) return Stream.empty();

        return Streams.stream(new Iterator<String>() {
            private String current = string;

            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            public String next() {
                if (current == null) {
                    throw new NoSuchElementException();
                }

                if (current.length() > length) {
                    String ret = current.substring(0, length);
                    current = current.substring(length);
                    return ret;
                } else {
                    String last = current;
                    current = null;
                    return last;
                }
            }
        });
    }
}

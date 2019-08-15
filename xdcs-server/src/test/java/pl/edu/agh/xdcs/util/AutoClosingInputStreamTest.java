package pl.edu.agh.xdcs.util;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Kamil Jarosz
 */
class AutoClosingInputStreamTest {
    @Test
    void testRead() throws IOException {
        InputStream mock = mock(InputStream.class);
        when(mock.read())
                .thenReturn(1)
                .thenReturn(2)
                .thenReturn(3)
                .thenReturn(-1);

        InputStream autoClosing = new AutoClosingInputStream(mock);

        assertThat(autoClosing.read()).isEqualTo(1);
        verify(mock, never()).close();
        assertThat(autoClosing.read()).isEqualTo(2);
        verify(mock, never()).close();
        assertThat(autoClosing.read()).isEqualTo(3);
        verify(mock, never()).close();
        assertThat(autoClosing.read()).isEqualTo(-1);
        verify(mock, times(1)).close();
        assertThat(autoClosing.read()).isEqualTo(-1);
        verify(mock, times(1)).close();
        autoClosing.close();
        verify(mock, times(1)).close();
    }

    @Test
    void restReadBytes() throws IOException {
        byte[] buffer = new byte[]{1, 2, 3, 4};
        InputStream delegate = new ByteArrayInputStream(buffer);
        InputStream mock = mock(InputStream.class);
        when(mock.read(any(), anyInt(), anyInt()))
                .thenAnswer(invocationOnMock -> delegate.read(invocationOnMock.getArgument(0),
                        invocationOnMock.getArgument(1),
                        invocationOnMock.getArgument(2)));

        InputStream autoClosing = new AutoClosingInputStream(mock);

        byte[] bytes;

        bytes = new byte[2];
        assertThat(autoClosing.read(bytes)).isEqualTo(2);
        assertThat(bytes).isEqualTo(new byte[]{1, 2});
        verify(mock, never()).close();

        bytes = new byte[1];
        assertThat(autoClosing.read(bytes)).isEqualTo(1);
        assertThat(bytes).isEqualTo(new byte[]{3});
        verify(mock, never()).close();

        bytes = new byte[4];
        assertThat(autoClosing.read(bytes)).isEqualTo(1);
        assertThat(bytes[0]).isEqualTo((byte) 4);
        verify(mock, never()).close();

        bytes = new byte[4];
        assertThat(autoClosing.read(bytes)).isEqualTo(-1);
        verify(mock, times(1)).close();
        assertThat(autoClosing.read(bytes)).isEqualTo(-1);
        verify(mock, times(1)).close();
        autoClosing.close();
        verify(mock, times(1)).close();
    }
}

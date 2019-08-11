package pl.edu.agh.xdcs.or.types;

import lombok.EqualsAndHashCode;
import pl.edu.agh.xdcs.or.ObjectBase;

/**
 * @author Kamil Jarosz
 */
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Blob implements ObjectBase {
    @EqualsAndHashCode.Include
    private byte[] data;

    Blob(byte[] data) {
        this.data = data;
    }

    public static Blob fromBytes(byte[] data) {
        return new Blob(data);
    }

    public byte[] getData() {
        return data;
    }
}

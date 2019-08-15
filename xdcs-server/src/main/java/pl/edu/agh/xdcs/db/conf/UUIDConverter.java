package pl.edu.agh.xdcs.db.conf;

import javax.persistence.AttributeConverter;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * @author Kamil Jarosz
 */
public class UUIDConverter implements AttributeConverter<String, BigInteger> {
    @Override
    public BigInteger convertToDatabaseColumn(String attribute) {
        UUID uuid = UUID.fromString(attribute);
        ByteBuffer buf = ByteBuffer.allocate(17);
        buf.put((byte) 0);
        buf.putLong(uuid.getMostSignificantBits());
        buf.putLong(uuid.getLeastSignificantBits());
        return new BigInteger(buf.array());
    }

    @Override
    public String convertToEntityAttribute(BigInteger dbData) {
        ByteBuffer buf = ByteBuffer.allocate(16);
        buf.put(dbData.toByteArray(), 1, 16);
        buf.rewind();
        return new UUID(buf.getLong(), buf.getLong()).toString();
    }
}

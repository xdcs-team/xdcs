package pl.edu.agh.xdcs.db.conf;

import javax.persistence.AttributeConverter;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author Krystian Życiński
 */
public class InetAddressConverter implements AttributeConverter<InetAddress, String> {
    @Override
    public String convertToDatabaseColumn(InetAddress inetAddress) {
        if (inetAddress == null) {
            return null;
        }
        return inetAddress.getHostAddress();
    }

    @Override
    public InetAddress convertToEntityAttribute(String s) {
        if (s == null) {
            return null;
        }

        try {
            return InetAddress.getByName(s);
        } catch (UnknownHostException e) {
            throw new RuntimeException("Cannot convert to InetAddress: " + s, e);
        }
    }
}

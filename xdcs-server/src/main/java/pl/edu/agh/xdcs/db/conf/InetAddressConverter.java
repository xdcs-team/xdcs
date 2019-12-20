package pl.edu.agh.xdcs.db.conf;

import org.slf4j.Logger;

import javax.inject.Inject;
import javax.persistence.AttributeConverter;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author Krystian Życiński
 */
public class InetAddressConverter implements AttributeConverter<InetAddress, String> {
    @Inject
    private Logger logger;

    @Override
    public String convertToDatabaseColumn(InetAddress inetAddress) {
        if(inetAddress == null) return null;
        return inetAddress.getHostAddress();

    }

    @Override
    public InetAddress convertToEntityAttribute(String s) {
        if(s == null) return null;
        try {
            return InetAddress.getByName(s);
        } catch (UnknownHostException e) {
            logger.error("Cannot convert to InetAddress: " + s);
        }
        return null;
    }
}

package pl.edu.agh.xdcs.db.conf;

import javax.persistence.AttributeConverter;
import java.util.regex.Pattern;

/**
 * @author Kamil Jarosz
 */
public class PatternConverter implements AttributeConverter<Pattern, String> {
    @Override
    public String convertToDatabaseColumn(Pattern attribute) {
        return attribute.toString();
    }

    @Override
    public Pattern convertToEntityAttribute(String dbData) {
        return Pattern.compile(dbData);
    }
}

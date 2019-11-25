package pl.edu.agh.xdcs.db.conf;

import pl.edu.agh.xdcs.util.WildcardPattern;

import javax.persistence.AttributeConverter;

/**
 * @author Kamil Jarosz
 */
public class WildcardPatternConverter implements AttributeConverter<WildcardPattern, String> {
    @Override
    public String convertToDatabaseColumn(WildcardPattern attribute) {
        return attribute.toString();
    }

    @Override
    public WildcardPattern convertToEntityAttribute(String dbData) {
        return WildcardPattern.parse(dbData);
    }
}

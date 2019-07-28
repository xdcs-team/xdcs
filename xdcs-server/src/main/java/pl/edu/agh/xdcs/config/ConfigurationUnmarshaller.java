package pl.edu.agh.xdcs.config;

import org.xml.sax.SAXException;

import javax.annotation.PostConstruct;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.InputStream;
import java.net.URL;
import java.util.Optional;

/**
 * @author Kamil Jarosz
 */
public class ConfigurationUnmarshaller {
    private Unmarshaller unmarshaller;

    @PostConstruct
    public void initialize() {
        try {
            JAXBContext context = JAXBContext.newInstance(ObjectFactory.class);

            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = sf.newSchema(getSchemaUrl());

            unmarshaller = context.createUnmarshaller();
            unmarshaller.setSchema(schema);
        } catch (JAXBException | SAXException e) {
            throw new RuntimeException(e);
        }
    }

    private URL getSchemaUrl() {
        return Optional.ofNullable(Thread.currentThread().getContextClassLoader().getResource("config_1.0.xsd"))
                .orElseThrow(() -> new RuntimeException("XSD config schema not found"));
    }

    @SuppressWarnings("unchecked")
    public Configuration unmarshal(InputStream configXml) throws JAXBException {
        return ((JAXBElement<Configuration>) unmarshaller.unmarshal(configXml)).getValue();
    }
}

package pl.edu.agh.xdcs.config.adapters;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author Kamil Jarosz
 */
public class XmlURIAdapter {
    public static URI parse(String uri) {
        try {
            return new URI(uri);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}

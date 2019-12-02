package pl.edu.agh.xdcs.ws.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.inject.Inject;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

/**
 * @author Kamil Jarosz
 */
public class JsonEncoder implements Encoder.Text<Object> {
    @Inject
    private ObjectMapper objectMapper;

    @Override
    public void init(EndpointConfig ec) {
        // no need to initialize
    }

    @Override
    public void destroy() {
        // no need to destroy
    }

    @Override
    public String encode(Object message) throws EncodeException {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new EncodeException(message, "Failed to serialize object", e);
        }
    }
}

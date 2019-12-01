package pl.edu.agh.xdcs.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.CloseReason;
import javax.websocket.Session;
import java.io.IOException;

/**
 * @author Kamil Jarosz
 */
public class WsUtils {
    private static final Logger logger = LoggerFactory.getLogger(WsUtils.class);

    public static void tryClose(Session session, CloseReason reason) {
        try {
            session.close(reason);
        } catch (IOException ex) {
            logger.error("Error closing WebSocket session", ex);
        }
    }
}

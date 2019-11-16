package pl.edu.agh.xdcs.test.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Kamil Jarosz
 */
public class TestLogger {
    private static final Logger LOGGER = LoggerFactory.getLogger("TestLogger");

    public static Logger getLogger() {
        return LOGGER;
    }
}

package pl.edu.agh.xdcs.util;

import java.nio.file.Path;

/**
 * @author Kamil Jarosz
 */
public class PathTraversalDetectedException extends RuntimeException {
    public PathTraversalDetectedException(Path original, Path invalid) {
        super("Detected path traversal from " + original + " to " + invalid);
    }
}

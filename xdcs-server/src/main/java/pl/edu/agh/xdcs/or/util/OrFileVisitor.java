package pl.edu.agh.xdcs.or.util;

import pl.edu.agh.xdcs.or.types.Tree;

/**
 * @author Kamil Jarosz
 */
public interface OrFileVisitor {
    void visitEntry(String path, Tree.Entry entry);

    default void beforeVisitDirectory(String path, Tree.Entry entry) {

    }

    default void afterVisitDirectory(String path, Tree.Entry entry) {

    }
}

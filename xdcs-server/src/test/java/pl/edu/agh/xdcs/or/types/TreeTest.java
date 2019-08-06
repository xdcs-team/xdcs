package pl.edu.agh.xdcs.or.types;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Kamil Jarosz
 */
class TreeTest {
    private static final String OBJID = "1234567890123456789012345678901234567890";

    @Test
    void testEntriesSorting() {
        Tree.Entry a = Tree.Entry.of(Tree.EntryMode.fromString("100777"), "a", OBJID);
        Tree.Entry b = Tree.Entry.of(Tree.EntryMode.fromString("100777"), "b", OBJID);
        Tree.Entry ab = Tree.Entry.of(Tree.EntryMode.fromString("100777"), "ab", OBJID);
        Tree tree = Tree.ofEntries(ab, b, a);
        assertThat(tree.getEntries())
                .containsExactly(a, ab, b);
    }
}

package net.tangly.dev.model;

import java.util.List;

import net.tangly.dev.apps.ParserGitCommits;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class CommitGitTest {
    private static final String COMMIT = "[03c6422] marcelbaumann 2019-03-23 feat - new baseline of the tangly-os repository";
    private static final String COMMIT_ITEM_1 ="127      11      moduleA/src/main/java/packageA/ClassA.java";
    private static final String COMMIT_ITEM_2 ="100     200      moduleA/src/main/java/packageA/ClassB.java";

    @Test
    void testCommitOf() {
        ParserGitCommits parser = new ParserGitCommits();
        Commit item = parser.of(List.of(COMMIT, COMMIT_ITEM_1, COMMIT_ITEM_2));

        assertThat(item).isNotNull();
        assertThat(item.items().size()).isEqualTo(2);

        assertThat(item.items().get(0).addedLines()).isEqualTo(127);
        assertThat(item.items().get(0).removedLines()).isEqualTo(11);
        assertThat(item.items().get(0).commit()).isEqualTo(item);

        assertThat(item.items().get(1).addedLines()).isEqualTo(100);
        assertThat(item.items().get(1).removedLines()).isEqualTo(200);
        assertThat(item.items().get(1).commit()).isEqualTo(item);
    }
}

package net.tangly.dev.model;

import net.tangly.dev.apps.ParserGitCommits;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class CommitGitTest {
    private static final String COMMIT = "[03c6422] marcelbaumann 2019-03-23 feat - new baseline of the tangly-os repository";
    private static final String COMMIT_ITEM_1 ="127      11      moduleA/src/main/java/packageA/ClassA.java";
    private static final String COMMIT_ITEM_2 ="100     200      moduleA/src/main/java/packageA/ClassB.java";

    @Test
    void testCommitOf() {
        Commit item = ParserGitCommits.of(List.of(COMMIT));
        assertThat(item).isNotNull();
    }

    @Test
    void testCommitOf2() {
        Commit item = ParserGitCommits.of(List.of(COMMIT, COMMIT_ITEM_1, COMMIT_ITEM_2));
        assertThat(item).isNotNull();

    }

    @Test
    void testCommitItemOf() {
        CommitItem item = ParserGitCommits.of(COMMIT_ITEM_1);
        assertThat(item).isNotNull();
        assertThat(item.filename()).isNotBlank();
        assertThat(item.addedLines()).isEqualTo(127);
        assertThat(item.removedLines()).isEqualTo(11);
    }
}

/*
 * Copyright 2006-2020 Marcel Baumann
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain
 *  a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 *  under the License.
 */

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

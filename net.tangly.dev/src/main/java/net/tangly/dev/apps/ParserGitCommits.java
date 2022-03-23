/*
 * Copyright 2006-2022 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.dev.apps;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.tangly.dev.model.Commit;
import net.tangly.dev.model.CommitItem;
import net.tangly.dev.model.Committer;
import net.tangly.dev.model.RepositoryFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * <p><code>git log --numstat --pretty=format:'[%h] %an %ad %s' --date=short</code></p>
 * <p><code>pmd pmd -d . -R ./src/main/resources/pmd-codescene.xml -f csv &gt; test.txt</code></p>
 * THe parser builds the structure of repository files, committers, commits and individual file changes over a period of time.
 */
public class ParserGitCommits {
    private static final Logger logger = LogManager.getLogger();
    private final Set<Committer> committers;
    private final List<RepositoryFile> files;
    private final List<Commit> commits;

    public ParserGitCommits() {
        committers = new HashSet<>();
        files = new ArrayList<>();
        commits = new ArrayList<>();
    }

    public CommitItem of(Commit commit, String line) {
        String[] tokens = line.split("(\\s+)", 3);
        RepositoryFile file = findOrCreateRepositoryFile(tokens[2]);
        CommitItem item = new CommitItem(commit, file, Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]));
        commit.add(item);
        return item;
    }

    public Commit of(String line) {
        String[] tokens = line.split(" ", 4);
        Committer committer = findOrCreateCommitter(tokens[1]);
        return new Commit(tokens[0], committer, LocalDate.parse(tokens[2]), tokens[3]);
    }

    public Commit of(List<String> lines) {
        String[] tokens = lines.get(0).split(" ", 4);
        Committer committer = findOrCreateCommitter(tokens[1]);
        Commit commit = new Commit(tokens[0], committer, LocalDate.parse(tokens[2]), tokens[3]);
        lines.stream().skip(1).forEach(o -> of(commit, o));
        return commit;
    }


    public Set<Committer> committers() {
        return Collections.unmodifiableSet(committers);
    }

    public List<RepositoryFile> files() {
        return Collections.unmodifiableList(files);
    }

    public List<Commit> commits() {
        return Collections.unmodifiableList(commits);
    }

    public void parseGitLog(@NotNull Path logs) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(logs, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                while (line.isBlank()) {
                    line = reader.readLine();
                }
                Commit commit = of(line);
                line = reader.readLine();
                while (!line.isBlank()) {
                    of(commit, line);
                }
                commits.add(commit);
            }
        } catch (IOException e) {
            logger.atError().withThrowable(e).log("Error when reading file");
        }
    }

    public Committer findOrCreateCommitter(String alias) {
        return committers.stream().filter(o -> alias.equals(o.name())).findAny().orElseGet(() -> createCommitter(alias));
    }

    public Committer createCommitter(String name) {
        Committer committer = new Committer(name);
        committers.add(committer);
        return committer;
    }

    public RepositoryFile findOrCreateRepositoryFile(String qualifiedName) {
        return files.stream().filter(o -> qualifiedName.equals(o.name())).findAny().orElseGet(() -> createRepositoryFile(qualifiedName));
    }

    public RepositoryFile createRepositoryFile(String qualifiedName) {
        RepositoryFile file = new RepositoryFile(qualifiedName);
        files.add(file);
        return file;
    }
}

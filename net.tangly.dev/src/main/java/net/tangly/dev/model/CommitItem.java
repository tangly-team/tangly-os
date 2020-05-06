package net.tangly.dev.model;

import org.jetbrains.annotations.NotNull;

public record CommitItem(@NotNull Commit commit, @NotNull RepositoryFile file, int addedLines, int removedLines) {
}

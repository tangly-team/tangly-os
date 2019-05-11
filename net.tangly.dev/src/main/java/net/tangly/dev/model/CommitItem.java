package net.tangly.dev.model;

import org.jetbrains.annotations.NotNull;

public class CommitItem {
    private final Commit commit;
    private final RepositoryFile file;
    private int addedLines;
    private int removedLines;

    public CommitItem(Commit commit, @NotNull RepositoryFile file, int addedLines, int removedLines) {
        this.commit = commit;
        this.file = file;
        this.addedLines = addedLines;
        this.removedLines = removedLines;
    }

    public RepositoryFile filename() {
        return file;
    }

    public Commit commit() {
        return commit;
    }

    public int addedLines() {
        return addedLines;
    }

    public int removedLines() {
        return removedLines;
    }
}

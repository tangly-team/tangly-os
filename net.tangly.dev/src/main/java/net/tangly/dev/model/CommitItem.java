package net.tangly.dev.model;

import org.jetbrains.annotations.NotNull;

public class CommitItem {
    private String filename;
    private int addedLines;
    private int removedLines;

    public CommitItem(@NotNull String filename, int addedLines, int removedLines) {
        this.filename = filename;
        this.addedLines = addedLines;
        this.removedLines = removedLines;
    }

    public String filename() {
        return filename;
    }

    public int addedLines() {
        return addedLines;
    }

    public int removedLines() {
        return removedLines;
    }
}

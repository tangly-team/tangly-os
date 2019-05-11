package net.tangly.dev.model;

import net.tangly.commons.utilities.Preconditions;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Commit {
    private final String id;
    private final Committer committer;
    private final LocalDate timestamp;
    private final String comment;
    private final List<CommitItem> items;

    public Commit(String id, Committer committer, LocalDate timestamp, String comment) {
        this.id = id;
        this.committer = committer;
        this.timestamp = timestamp;
        this.comment = comment;
        items = new ArrayList<>();
    }

    public String id() {
        return id;
    }

    public Committer committer() {
        return committer;
    }

    public LocalDate timestamp() {
        return timestamp;
    }

    public String comment() {
        return comment;
    }

    public void add(CommitItem item) {
        Preconditions.checkArgument(item.commit() == this);
        items.add(item);
    }

    public List<CommitItem> items() {
        return Collections.unmodifiableList(items);
    }
}

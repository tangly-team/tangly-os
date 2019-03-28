package net.tangly.dev.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Commit {
    private String id;
    private String committer;
    private LocalDate timestamp;
    private String comment;
    private List<CommitItem> items;

    public Commit(String id, String committer, LocalDate timestamp, String comment) {
        this.id = id;
        this.committer = committer;
        this.timestamp = timestamp;
        this.comment = comment;
        items = new ArrayList<>();
    }

    public String id() {
        return id;
    }

    public String committer() {
        return committer;
    }

    public LocalDate timestamp() {
        return timestamp;
    }

    public String comment() {
        return comment;
    }
}

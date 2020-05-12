package net.tangly.dev.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RepositoryFile {
    private String name;
    private List<CommitItem> changes;

    public RepositoryFile() {
        changes = new ArrayList<>();
    }
    public RepositoryFile(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    public void add(CommitItem change) {
        changes.add(change);
    }

    public List<CommitItem> changes() {
        return Collections.unmodifiableList(changes);
    }
}

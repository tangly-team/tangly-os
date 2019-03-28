package net.tangly.dev.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Project {
    Set<Committer> committers;
    List<RepositoryFile> files;
    List<Module> modules;
    List<Commit> commits;

    public Project() {
        committers = new HashSet<>();
        files = new ArrayList<>();
        modules = new ArrayList<>();
        commits = new ArrayList<>();
    }
}

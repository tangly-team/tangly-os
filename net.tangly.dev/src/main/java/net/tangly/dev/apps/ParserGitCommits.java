package net.tangly.dev.apps;

import net.tangly.dev.model.Commit;
import net.tangly.dev.model.CommitItem;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

/**
 * <code>git log --numstat --pretty=format:'[%h] %an %ad %s' --date=short</code>
 */
public class ParserGitCommits {
    public static CommitItem of(String line) {
        String[] tokens = line.split("(\\s+)", 3);
        return new CommitItem(tokens[2].trim(), Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]));
    }

    public static Commit of(List<String> lines) {
        String[] tokens = lines.get(0).split(" ", 4);
        Commit commit= new Commit(tokens[0], tokens[1], LocalDate.parse(tokens[2]), tokens[3]);
        lines.stream().skip(1).forEach(o -> of(o));
        return commit;
    }

    public void parseGitLog(Path logs) {}
}

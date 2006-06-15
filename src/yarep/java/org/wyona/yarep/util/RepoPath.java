package org.wyona.yarep.util;

import org.apache.log4j.Category;

import org.wyona.yarep.core.Path;
import org.wyona.yarep.core.Repository;

/**
 *
 */
public class RepoPath {
    private Repository repo;
    private Path path;

    /**
     *
     */
    public RepoPath(Repository repo, Path path) {
        this.repo = repo;
        this.path = path;
    }

    /**
     *
     */
    public Repository getRepo() {
        return repo;
    }

    /**
     *
     */
    public Path getPath() {
        return path;
    }
}

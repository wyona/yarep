package org.wyona.yarep.core.impl.fs;

import org.wyona.yarep.core.Path;
import org.wyona.yarep.core.AbstractRepository;
import org.wyona.yarep.core.Repository;
import org.wyona.yarep.core.io.RepositoryWriter;
import org.wyona.yarep.core.io.RepositoryReader;

import java.io.File;

/**
 *
 */
public class FileSystemRepository extends AbstractRepository implements Repository {

    /**
     *
     */
    public FileSystemRepository(String id, File config) {
        super(id, config);
    }

    /**
     *
     */
    public RepositoryWriter getWriter(Path path) {
        return null;
        //return FileSystemRepositoryWriter(path);
    }

    /**
     *
     */
    public RepositoryReader getReader(Path path) {
        return null;
        //return FileSystemRepositoryReader(path);
    }
}

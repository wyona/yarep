package org.wyona.yarep.core;

import java.io.File;

/**
 *
 */
public abstract class AbstractRepository {

    protected String id;
    protected File config;

    /**
     *
     */
    public AbstractRepository(String id, File config) {
        this.id = id;
        this.config = config;
    }

    /**
     *
     */
    public String toString() {
        return "Repository: ID = " + id + ", Configuration-File = " + config;
    }
}

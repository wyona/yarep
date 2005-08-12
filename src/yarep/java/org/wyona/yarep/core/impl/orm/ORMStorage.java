package org.wyona.yarep.core.impl.orm;

import org.wyona.yarep.core.Path;
import org.wyona.yarep.core.Storage;
import org.wyona.yarep.core.UID;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.log4j.Category;

/**
 *
 */
public class ORMStorage implements Storage {

    private static Category log = Category.getInstance(ORMStorage.class);

    /**
     *
     */
    public void readConfig(Configuration storageConfig, File repoConfigFile) {
    }

    /**
     *
     */
    public Writer getWriter(UID uid, Path path) {
        log.warn("Not implemented yet!");
        return null;
    }

    /**
     *
     */
    public OutputStream getOutputStream(UID uid, Path path) {
        log.warn("Not implemented yet!");
        return null;
    }

    /**
     *
     */
    public Reader getReader(UID uid, Path path) {
        log.warn("Not implemented yet!");
        return null;
    }

    /**
     *
     */
    public InputStream getInputStream(UID uid, Path path) {
        log.warn("Not implemented yet!");
        return null;
    }

    /**
     *
     */
    public long getLastModified(UID uid, Path path) {
        log.warn("Not implemented yet!");
        return 0;
    }
}

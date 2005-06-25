package org.wyona.yarep.core;

import org.apache.avalon.framework.configuration.Configuration;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;

/**
 *
 */
public interface Storage {

    /**
     *
     */
    public void readConfig(Configuration storageConfig, File repoConfigFile);

    /**
     *
     */
    public Writer getWriter(UID uid, Path path);

    /**
     *
     */
    public Reader getReader(UID uid, Path path);

    /**
     *
     */
    public InputStream getInputStream(UID uid, Path path);
}

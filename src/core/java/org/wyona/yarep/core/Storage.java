package org.wyona.yarep.core;

import org.apache.avalon.framework.configuration.Configuration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

/**
 *
 */
public interface Storage {

    /**
     *
     */
    public void readConfig(Configuration storageConfig, File repoConfigFile) throws Exception;

    /**
     *@deprecated
     */
    public Writer getWriter(UID uid, Path path);

    /**
     *
     */
    public OutputStream getOutputStream(UID uid, Path path) throws IOException;

    /**
     *@deprecated
     */
    public Reader getReader(UID uid, Path path) throws NoSuchNodeException;

    /**
     *
     */
    public InputStream getInputStream(UID uid, Path path) throws IOException;

    /**
     *
     */
    public long getLastModified(UID uid, Path path);

    /**
     *
     */
    public boolean delete(UID uid, Path path);
}

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
    public void readConfig(Configuration storageConfig, File repoConfigFile) throws RepositoryException;

    /**
     *@deprecated
     */
    public Writer getWriter(UID uid, Path path);

    /**
     *
     */
    public OutputStream getOutputStream(UID uid, Path path) throws RepositoryException;

    /**
     *@deprecated
     */
    public Reader getReader(UID uid, Path path) throws RepositoryException;

    /**
     *
     */
    public InputStream getInputStream(UID uid, Path path) throws RepositoryException;

    /**
     *
     */
    public long getLastModified(UID uid, Path path) throws RepositoryException;
    
    /**
     * 
     */
    public long getSize(UID uid, Path path) throws RepositoryException;

    /**
     *
     */
    public boolean delete(UID uid, Path path) throws RepositoryException;
    
    /**
     *
     */
    public String[] getRevisions(UID uid, Path path) throws RepositoryException;

}

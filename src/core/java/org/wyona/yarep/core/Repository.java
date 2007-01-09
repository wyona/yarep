package org.wyona.yarep.core;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

/**
 *
 */
public interface Repository {

    /**
     * Get repository ID
     */
    public String getID();

    /**
     * Get repository name
     */
    public String getName();

    /**
     * Get repository configuration file
     */
    public File getConfigFile();

    /**
     *
     */
    public Writer getWriter(Path path) throws RepositoryException;

    /**
     *
     */
    public OutputStream getOutputStream(Path path) throws RepositoryException;

    /**
     *
     */
    public Reader getReader(Path path) throws RepositoryException;

    /**
     *
     */
    public InputStream getInputStream(Path path) throws RepositoryException;

    /**
     *
     */
    public long getLastModified(Path path) throws RepositoryException;
    
    /**
     *
     */
    public long getSize(Path path) throws RepositoryException;
    
    /**
     * @return true if node has been deleted, otherwise false
     */
    public boolean delete(Path path) throws RepositoryException;

    /**
     * http://excalibur.apache.org/apidocs/org/apache/excalibur/source/impl/FileSource.html#getValidity()
     * http://excalibur.apache.org/apidocs/org/apache/excalibur/source/SourceValidity.html
     */
    public void getValidity(Path path) throws RepositoryException;

    /**
     * http://excalibur.apache.org/apidocs/org/apache/excalibur/source/impl/FileSource.html#getContentLength()
     */
    public void getContentLength(Path path) throws RepositoryException;

    /**
     * http://excalibur.apache.org/apidocs/org/apache/excalibur/source/impl/FileSource.html#getURI()
     */
    public void getURI(Path path) throws RepositoryException;

    /**
     *
     */
    public boolean isResource(Path path) throws RepositoryException;

    /**
     * One might want to discuss what is a collection. A resource for instance could
     * also be a collection, but a collection with some default content.
     * In the case of JCR there are only nodes and properties!
     */
    public boolean isCollection(Path path) throws RepositoryException;

    /**
     *
     */
    public boolean exists(Path path) throws RepositoryException;

    /**
     *
     */
    public Path[] getChildren(Path path) throws RepositoryException;

    /**
     * Get UID
     *
     * http://www.ietf.org/rfc/rfc4122.txt
     * http://incubator.apache.org/jackrabbit/apidocs/org/apache/jackrabbit/uuid/UUID.html
     * http://www.webdav.org/specs/draft-leach-uuids-guids-01.txt
     */
    public UID getUID(Path path) throws RepositoryException;
    
    /**
     * Get all revision numbers of the given path.
     * @return Array of revision number strings. Newest revision first. 
     */
   public String[] getRevisions(Path path) throws RepositoryException;

    /**
     * Add symbolic link
     */
    public void addSymbolicLink(Path target, Path link) throws RepositoryException;
}

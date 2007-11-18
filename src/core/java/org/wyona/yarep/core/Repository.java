package org.wyona.yarep.core;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

/**
 * The repository interface has two sets of methods for historical reasons:
 * <ul>
 * <li>path based like repo.getInputStream(Path path)</li>
 * <li>node based like repo.getNode(String patHh).getInputStream()</li>
 * </ul>
 * The path based methods are deprecated, please use the node based methods instead.
 * <br/><br/>
 * The node based repository is composed of a hierarchy of nodes and properties 
 * with the following characteristics:
 * <ul> 
 * <li>A node has a path and a uuid (universally unique identifier).
 *     Either the path or the uuid may be used to identify a node.
 *     The uuid is immutable, but the path may change when a node is moved around.
 * </li>
 * <li>A node has a type, either collection or resource.</li>
 * <li>A node of type 'collection' may have subnodes, but has no content.</li>
 * <li>A node of type 'resource' may not have subnodes, but has content.</li>
 * <li>A node of either type may have properties</li>
 * <ul>
 * <li>A property of a node is identified by a name</li>
 * <li>A property of a node contains a value of a certain type 
 *     (string, boolean, date, long, double)</li>
 * <li>Binary properties are currently not supported yet.</li>
 * </ul>
 * <li>TODO: explain checkin/checkout</li>
 * <li>TODO: explain versioning</li>
 * </ul>
 * @see org.wyona.yarep.core.Node
 * @see org.wyona.yarep.core.Property
 * @see org.wyona.yarep.core.Revision
 */
public interface Repository {

    /**
     * Get repository ID
     */
    public String getID();

    /**
     * Set repository ID
     */
    public void setID(String id);

    /**
     * Read configuration
     */
    public void readConfiguration(File configFile) throws RepositoryException;

    /**
     * Get repository name
     */
    public String getName();

    /**
     * Get repository configuration file
     */
    public File getConfigFile();

    /**
     * @deprecated
     */
    public Writer getWriter(Path path) throws RepositoryException;

    /**
     * @deprecated
     */
    public OutputStream getOutputStream(Path path) throws RepositoryException;

    /**
     * @deprecated
     */
    public Reader getReader(Path path) throws RepositoryException;

    /**
     * @deprecated
     */
    public InputStream getInputStream(Path path) throws RepositoryException;

    /**
     * @deprecated
     */
    public long getLastModified(Path path) throws RepositoryException;
    
    /**
     * @deprecated
     */
    public long getSize(Path path) throws RepositoryException;
    
    /**
     * @return true if node has been deleted, otherwise false
     * @deprecated
     */
    public boolean delete(Path path) throws RepositoryException;
    
    /**
     * In order to allow deleting collections
     *
     * @return true if node has been deleted, otherwise false
     * @deprecated
     */
    public boolean delete(Path path, boolean recursive) throws RepositoryException;

    /**
     * http://excalibur.apache.org/apidocs/org/apache/excalibur/source/impl/FileSource.html#getValidity()
     * http://excalibur.apache.org/apidocs/org/apache/excalibur/source/SourceValidity.html
     * @deprecated
     */
    public void getValidity(Path path) throws RepositoryException;

    /**
     * http://excalibur.apache.org/apidocs/org/apache/excalibur/source/impl/FileSource.html#getContentLength()
     * @deprecated
     */
    public void getContentLength(Path path) throws RepositoryException;

    /**
     * http://excalibur.apache.org/apidocs/org/apache/excalibur/source/impl/FileSource.html#getURI()
     * @deprecated
     */
    public void getURI(Path path) throws RepositoryException;

    /**
     * @deprecated
     */
    public boolean isResource(Path path) throws RepositoryException;

    /**
     * One might want to discuss what is a collection. A resource for instance could
     * also be a collection, but a collection with some default content.
     * In the case of JCR there are only nodes and properties!
     * @deprecated
     */
    public boolean isCollection(Path path) throws RepositoryException;

    /**
     * @deprecated
     */
    public boolean exists(Path path) throws RepositoryException;

    /**
     * @deprecated
     */
    public Path[] getChildren(Path path) throws RepositoryException;

    /**
     * Get UID
     *
     * http://www.ietf.org/rfc/rfc4122.txt
     * http://incubator.apache.org/jackrabbit/apidocs/org/apache/jackrabbit/uuid/UUID.html
     * http://www.webdav.org/specs/draft-leach-uuids-guids-01.txt
     * @deprecated
     */
    public UID getUID(Path path) throws RepositoryException;
    
    /**
     * Get all revision numbers of the given path.
     * @return Array of revision number strings. Newest revision first. 
     * @deprecated
     */
   public String[] getRevisions(Path path) throws RepositoryException;

    /**
     * Add symbolic link
     * @deprecated
     */
    public void addSymbolicLink(Path target, Path link) throws RepositoryException;
    

    
    ///////////////////////////////////////////////////////////////////////////
    // New methods for node based repository 
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Gets the node with the given path.
     * @param path absolute path
     * @return node
     * @throws NoSuchNodeException if node does not exist
     * @throws RepositoryException other error
     */
    public Node getNode(String path) throws NoSuchNodeException, RepositoryException;
    
    /**
     * Gets the node with the given uuid.
     * @param uuid
     * @return node
     * @throws NoSuchNodeException if node does not exist
     * @throws RepositoryException other error
     */
    public Node getNodeByUUID(String uuid) throws NoSuchNodeException, RepositoryException;
    
    /**
     * Indicates whether the node given by the path exists in this repository.
     * @param path absolute path
     * @return true if node exists
     * @throws RepositoryException repository error
     */
    public boolean existsNode(String path) throws RepositoryException;
    
    /**
     * Gets the root node of this repository.
     * @return root node
     * @throws RepositoryException repository error
     */
    public Node getRootNode() throws RepositoryException;
    
    /**
     * Copies the node given by srcPath to destPath, including its subtree.
     * destPath must not exist yet, but the parent must exist.
     * @param srcPath
     * @param destPath
     * @throws RepositoryException repository error
     */
    public void copy(String srcPath, String destPath) throws RepositoryException;
    
    /**
     * Moves the node given by srcPath to destPath, including its subtree.
     * destPath must not exist yet, but the parent must exist.
     * @param srcPath
     * @param destPath
     * @throws RepositoryException repository error
     */
    public void move(String srcPath, String destPath) throws RepositoryException;

    /**
     * Search content
     */
    //public Node[] search(String query) throws RepositoryException;

    /**
     * Search values of a specific property
     */
    //public Node[] searchProperty(String pName, String query) throws RepositoryException;

    /**
     * Closes the repository and releases any resources associated with the repository
     */
    public void close() throws RepositoryException;
}

package org.wyona.yarep.core;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

/**
 * This class represents a repository node.
 * A repository node may be either a collection ("directory") or a resource ("file").
 * If it is a resource, it has a binary default property, which may be accessed by using 
 * getInputStream() and getOutputStream().
 */
public interface Node {

    /**
     * Gets the name of this node, i.e. the last part of the path.
     * @return name
     * @throws RepositoryException repository error
     */
    public String getName() throws RepositoryException;
    
    /**
     * Gets the parent node of this node.
     * @return parent node or null if this is the root node
     * @throws RepositoryException repository error
     */
    public Node getParent() throws RepositoryException;
    
    /**
     * Removes this node and all subnodes.
     * @throws RepositoryException repository error
     */
    public void remove() throws RepositoryException;
    
    /**
     * Gets the complete repository path of this node.
     * @return path
     * @throws RepositoryException repository error
     */
    public String getPath() throws RepositoryException;
    
    /**
     * Gets the UUID of this node.
     * @return uuid
     * @throws RepositoryException repository error
     */
    public String getUUID() throws RepositoryException;
    
    /**
     * Gets the type of this node (collection or resource).
     * @return type
     * @throws RepositoryException repository error
     */
    public int getType() throws RepositoryException;
    
    /**
     * Indicates whether this node is of type "resource".
     * @return true if type is resource
     * @throws RepositoryException repository error
     */
    public boolean isResource() throws RepositoryException;
    
    /**
     * Indicates whether this node is of type "collection".
     * @return true if type is collection
     * @throws RepositoryException repository error
     */
    public boolean isCollection() throws RepositoryException;
    
    /**
     * Creates a new node and adds it as a child to this node.
     * @param name name of the child node
     * @parem type node type of the child node
     * @return the new child node
     * @throws RepositoryException repository error
     */
    public Node addNode(String name, int type) throws RepositoryException;
    
    /**
     * Gets the child node with the given name. Must be a direct child.
     * @param name
     * @return
     * @throws NoSuchNodeException if no child node with this name exists.
     * @throws RepositoryException repository error
     */
    public Node getNode(String name) throws NoSuchNodeException, RepositoryException;
    
    /**
     * Gets all child nodes.
     * @return child nodes or empty array if there are no child nodes.
     * @throws RepositoryException repository error
     */
    public Node[] getNodes() throws RepositoryException;
    
    /**
     * Indicates whether this node has a child node with the given name.
     * @param name
     * @return
     * @throws RepositoryException repository error
     */
    public boolean hasNode(String name) throws RepositoryException;
    
    /**
     * Gets the property with the given name.
     * @param name
     * @return
     * @throws NoSuchPropertyException if the property does not exist.
     * @throws RepositoryException other error
     */
    public Property getProperty(String name) throws NoSuchPropertyException, RepositoryException;
    
    /**
     * Get all properties of this node
     * @return properties of this node or empty array if there are no properties.
     * @throws RepositoryException other error
     */
    public Property[] getProperties() throws RepositoryException;
    
    /**
     * Indicates whether this node has a property with the given name.
     * @param name
     * @return
     * @throws RepositoryException repository error
     */
    public boolean hasProperty(String name) throws RepositoryException;
    
    //public boolean hasProperties() throws RepositoryException;
    
    /**
     * Sets a property of type boolean or creates it if it does not exist yet.
     * @param name
     * @param value
     * @return
     * @throws RepositoryException repository error
     */
    public Property setProperty(String name, boolean value) throws RepositoryException;
    
    /**
     * Sets a property of type date or creates it if it does not exist yet.
     * @param name
     * @param value
     * @return
     * @throws RepositoryException repository error
     */
    public Property setProperty(String name, Date value) throws RepositoryException;
    
    /**
     * Sets a property of type double or creates it if it does not exist yet.
     * @param name
     * @param value
     * @return
     * @throws RepositoryException repository error
     */
    public Property setProperty(String name, double value) throws RepositoryException;
    
    //public Property setProperty(String name, InputStream value) throws RepositoryException;
    
    /**
     * Sets a property of type long or creates it if it does not exist yet.
     * @param name
     * @param value
     * @return
     * @throws RepositoryException repository error
     */
    public Property setProperty(String name, long value) throws RepositoryException;
    
    /**
     * Sets a property of type string or creates it if it does not exist yet.
     * @param name
     * @param value
     * @return
     * @throws RepositoryException repository error
     */
    public Property setProperty(String name, String value) throws RepositoryException;

    /**
     * Sets a property or creates it if it does not exist yet.
     * @param property
     * @throws RepositoryException repository error
     */
    public void setProperty(Property property) throws RepositoryException;

    //public Property getDefaultProperty() throws RepositoryException;
    
    /**
     * Gets an input stream of the binary default property.
     * Useful only for nodes of type resource.
     * @return
     * @throws RepositoryException repository error
     */
    public InputStream getInputStream() throws RepositoryException;
    
    //public void setInputStream(InputStream inputStream) throws RepositoryException;
    
    /**
     * Gets an output stream of the binary default property.
     * Useful only for nodes of type resource.
     * @return
     * @throws RepositoryException repository error
     */
    public OutputStream getOutputStream() throws RepositoryException;
    
    /**
     * Checks in this node and creates a new revision.
     * @return
     * @throws NodeStateException if node is not in checked out state
     * @throws RepositoryException repository error
     */
    public Revision checkin() throws NodeStateException, RepositoryException;
    
    /**
     * Checks out this node.
     * @throws NodeStateException if node is checked out by a different user
     * @throws RepositoryException repository error
     */
    public void checkout(String userID) throws NodeStateException, RepositoryException;
    
    /**
     * Indicates whether this node is checked out.
     * @return
     * @throws RepositoryException repository error
     */
    public boolean isCheckedOut() throws RepositoryException;
    
    /**
     * Gets the userID which was supplied when calling checkout(userID).
     * @return
     * @throws NodeStateException if node is not checked out.
     * @throws RepositoryException
     */
    public String getCheckoutUserID() throws NodeStateException, RepositoryException;
    
    /**
     * Gets all revisions of this node.
     * @return 
     * @throws RepositoryException
     */
    public Revision[] getRevisions() throws RepositoryException;
    
    /**
     * Gets the revision with the given name.
     * @param revisionName
     * @return
     * @throws NoSuchRevisionException if the revision does not exist
     * @throws RepositoryException
     */
    public Revision getRevision(String revisionName) throws NoSuchRevisionException, RepositoryException;
    
    /**
     * Gets the revision with the given name.
     * @param label
     * @return
     * @throws NoSuchRevisionException if the revision does not exist
     * @throws RepositoryException
     */
    public Revision getRevisionByLabel(String label) throws NoSuchRevisionException, RepositoryException;
    
    /**
     * Restores the revision with the given name.
     * @param revisionName
     * @throws NoSuchRevisionException if the revision does not exist
     * @throws RepositoryException
     */
    public void restore(String revisionName) throws NoSuchRevisionException, RepositoryException;
    
    /**
     * Gets the last modified date in ms of this node.
     * Changing a property should update the last modified date.
     * @return
     * @throws RepositoryException
     */
    public long getLastModified() throws RepositoryException;
    
    /**
     * Gets the size of the default property if this node is of type resource.
     * @return
     * @throws RepositoryException
     */
    public long getSize() throws RepositoryException;
    
    /**
     * Gets the mimetype of the default property of this node if this node is of type resource.
     * @return
     * @throws RepositoryException
     */
    public String getMimeType() throws RepositoryException;
    
    /**
     * Sets the mimetype of the default property of this node if this node is of type resource.
     * @param mimeType
     * @throws RepositoryException
     */
    public void setMimeType(String mimeType) throws RepositoryException;
    
    /**
     * Gets the encoding of the default property of this node if this node is of type resource.
     * @return
     * @throws RepositoryException
     */
    public String getEncoding() throws RepositoryException;
    
    /**
     * Sets the encoding of the default property of this node if this node is of type resource.
     * @param encoding
     * @throws RepositoryException
     */
    public void setEncoding(String encoding) throws RepositoryException;
        
}
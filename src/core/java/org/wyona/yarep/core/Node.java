package org.wyona.yarep.core;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

/**
 * This class represents a repository node.
 * A repository node may be either a collection ("directory") or a resource ("file").
 * If it is a resource, it has an associated data content, which may be accessed by using 
 * getInputStream()/getOutputStream().
 * To store textual data, the reader/writer methods should be used instead of the stream
 * methods to allow the implementation to handle textual data differently from binary data.
 * 
 * @see org.wyona.yarep.core.Repository
 */
public interface Node {

    /**
     * Gets the name of this node, which is the last part of the path.
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
     * Deletes this node and all subnodes.
     * The root node cannot be deleted.
     * @throws RepositoryException if this node is the root node or if a repository error occurs.
     */
    public void delete() throws RepositoryException;
    
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
     * @see org.wyona.yarep.core.NodeType
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
     * Indicates whether the content of this node is binary or textual.
     * Useful only if this node is a resource.
     * @return true if the content of this node is binary
     * @throws RepositoryException repository error
     */
    //public boolean isBinary() throws RepositoryException;
    
    /**
     * Creates a new node and adds it as a direct child to this node.
     * @param name name of the child node
     * @param type node type of the child node (e.g. collection or resource, see NodeType)
     * @return the new child node
     * @throws RepositoryException if this node is not a collection or if a repository error occurs
     */
    public Node addNode(String name, int type) throws RepositoryException;

    /**
     * Gets the child node with the given name. Must be a direct child.
     * @param name name of the child node
     * @return child node
     * @throws NoSuchNodeException if no child node with this name exists.
     * @throws RepositoryException if node is not a collection or if a repository error occurs
     */
    public Node getNode(String name) throws NoSuchNodeException, RepositoryException;
    
    /**
     * Gets all child nodes.
     *
     * There is no guarantee that the nodes in the resulting array will appear in any specific order; they are not, in particular, guaranteed to appear in alphabetical order.
     *
     * @return child nodes or empty array if there are no child nodes.
     * @throws RepositoryException if node is not a collection or if a repository error occurs
     */
    public Node[] getNodes() throws RepositoryException;
    
    /**
     * Indicates whether this node has a direct child node with the given name.
     * @param name
     * @return true if child node exists with the given id, false otherwise
     * @throws RepositoryException if node is not a collection or if a repository error occurs
     */
    public boolean hasNode(String name) throws RepositoryException;
    
    /**
     * Gets the property with the given name.
     * @param name
     * @return property or null if the property does not exist
     * @throws RepositoryException repository error
     */
    public Property getProperty(String name) throws RepositoryException;
    
    /**
     * Get all properties of this node
     * @return array of properties of this node or empty array if there are no properties.
     * @throws RepositoryException other error
     */
    public Property[] getProperties() throws RepositoryException;
    
    /**
     * Indicates whether this node has a property with the given name.
     * @param name
     * @return true if a property exists with the given name, false otherwise
     * @throws RepositoryException repository error
     */
    public boolean hasProperty(String name) throws RepositoryException;
    
    //public boolean hasProperties() throws RepositoryException;
    
    /**
     * Removes the property with the given name.
     * Does nothing if no property with the given name exists.
     * @param name
     * @throws RepositoryException repository error
     */
    public void removeProperty(String name) throws RepositoryException;
    
    /**
     * Sets a property of type boolean or creates it if it does not exist yet.
     * @param name
     * @param value
     * @return the set property
     * @throws RepositoryException repository error
     */
    public Property setProperty(String name, boolean value) throws RepositoryException;
    
    /**
     * Sets a property of type date or creates it if it does not exist yet.
     * @param name
     * @param value
     * @return the set property
     * @throws RepositoryException repository error
     */
    public Property setProperty(String name, Date value) throws RepositoryException;
    
    /**
     * Sets a property of type double or creates it if it does not exist yet.
     * @param name
     * @param value
     * @return the set property
     * @throws RepositoryException repository error
     */
    public Property setProperty(String name, double value) throws RepositoryException;
    
    //public Property setProperty(String name, InputStream value) throws RepositoryException;
    
    /**
     * Sets a property of type long or creates it if it does not exist yet.
     * @param name
     * @param value
     * @return the set property
     * @throws RepositoryException repository error
     */
    public Property setProperty(String name, long value) throws RepositoryException;
    
    /**
     * Sets a property of type string or creates it if it does not exist yet.
     * @param name
     * @param value
     * @return the set property
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
     * Gets an input stream of the binary data content of this node.
     * Useful only for nodes of type resource.
     * @return input stream
     * @throws RepositoryException repository error
     */
    public InputStream getInputStream() throws RepositoryException;
    
    //public void setInputStream(InputStream inputStream) throws RepositoryException;
    
    /**
     * Gets an output stream of the binary data content of this node.
     * Useful only for nodes of type resource.
     * Don't forget to close the stream because some implementations may
     * require that.
     * @return output stream
     * @throws RepositoryException repository error
     */
    public OutputStream getOutputStream() throws RepositoryException;
    
    /**
     * Puts this node into checked-in state and creates a new revision.
     * @return the new revision
     * @throws NodeStateException if node is not in checked out state
     * @throws RepositoryException repository error
     */
    public Revision checkin() throws NodeStateException, RepositoryException;
    
    /**
     * Puts this node into checked-in state and creates a new revision.
     * @param comment a comment to add to the new revision.
     * @return the new revision
     * @throws NodeStateException if node is not in checked out state
     * @throws RepositoryException repository error
     */
    public Revision checkin(String comment) throws NodeStateException, RepositoryException;
    
    /**
     * Puts this node into checked-out state.
     * @throws NodeStateException if node is in checked out state already
     * @throws RepositoryException repository error
     */
    public void checkout(String userID) throws NodeStateException, RepositoryException;
    
    /**
     * Cancels a checkout, i.e. performs a checkin without creating a new revision.
     * @throws NodeStateException
     * @throws NodeStateException if node is not in checked out state
     * @throws RepositoryException
     */
    public void cancelCheckout() throws NodeStateException, RepositoryException;
    
    /**
     * Indicates whether this node is checked out.
     * @return true if checked out, false otherwise
     * @throws RepositoryException repository error
     */
    public boolean isCheckedOut() throws RepositoryException;
    
    /**
     * Gets the userID which was supplied when calling checkout(userID).
     * @return userID
     * @throws NodeStateException if node is not checked out.
     * @throws RepositoryException
     */
    public String getCheckoutUserID() throws NodeStateException, RepositoryException;
    
    /**
     * Gets the date when this node was checked out.
     * @return checkout date
     * @throws NodeStateException if node is not checked out.
     * @throws RepositoryException
     */
    public Date getCheckoutDate() throws NodeStateException, RepositoryException;
    
    /**
     * Gets the date when this node was checked in.
     * @return checkin date
     * @throws NodeStateException if node is not checked in.
     * @throws RepositoryException
     */
    public Date getCheckinDate() throws NodeStateException, RepositoryException;
    
    /**
     * Gets all revisions of this node.
     * Oldest revision at the first array position, newest at the last position.
     * @return array of revisions, or empty array if there are no revisions
     * @throws RepositoryException
     */
    public Revision[] getRevisions() throws RepositoryException;
    
    /**
     * Gets the revision with the given name.
     * @param revisionName
     * @return revision
     * @throws NoSuchRevisionException if the revision does not exist
     * @throws RepositoryException
     */
    public Revision getRevision(String revisionName) throws NoSuchRevisionException, RepositoryException;
    
    /**
     * Gets the revision with the given tag.
     * If multiple revisions have the same tag, the oldest one will be returned.
     * @param tag
     * @return revision
     * @throws NoSuchRevisionException if the revision does not exist
     * @throws RepositoryException
     */
    public Revision getRevisionByTag(String tag) throws NoSuchRevisionException, RepositoryException;
    
    /**
     * Indicates whether this node has a revision with the given tag.
     * If multiple revisions have the same tag, the oldest one will be returned.
     * @param tag
     * @return true if a revision with the given tag exists, false otherwise
     * @throws RepositoryException
     */
    public boolean hasRevisionWithTag(String tag) throws RepositoryException;
    
    /**
     * Restores the revision with the given name.
     * @param revisionName
     * @throws NoSuchRevisionException if the revision does not exist
     * @throws RepositoryException
     */
    public void restore(String revisionName) throws NoSuchRevisionException, RepositoryException;
    
    /**
     * Gets the last modified date of this node in ms.
     * Changing a property should update the last modified date.
     * @return last modified date in ms
     * @throws RepositoryException
     */
    public long getLastModified() throws RepositoryException;
    
    /**
     * Gets the size of the data content of this node if this node is of type resource.
     * @return size in bytes
     * @throws RepositoryException
     */
    public long getSize() throws RepositoryException;
    
    /**
     * Gets the mimetype of the data content of this node if this node is of type resource.
     * @return mimetype
     * @throws RepositoryException
     */
    public String getMimeType() throws RepositoryException;
    
    /**
     * Sets the mimetype of the data content of this node if this node is of type resource.
     * @param mimeType
     * @throws RepositoryException
     */
    public void setMimeType(String mimeType) throws RepositoryException;
    
    /**
     * Gets the encoding of the data content of this node if this node is of type resource.
     * @return encoding
     * @throws RepositoryException
     */
    public String getEncoding() throws RepositoryException;
    
    /**
     * Sets the encoding of the data content of this node if this node is of type resource.
     * @param encoding
     * @throws RepositoryException
     */
    public void setEncoding(String encoding) throws RepositoryException;
}

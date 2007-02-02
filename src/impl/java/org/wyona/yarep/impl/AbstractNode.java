package org.wyona.yarep.impl;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.apache.log4j.Category;
import org.wyona.commons.io.PathUtil;
import org.wyona.yarep.core.NoSuchNodeException;
import org.wyona.yarep.core.NoSuchPropertyException;
import org.wyona.yarep.core.NoSuchRevisionException;
import org.wyona.yarep.core.Node;
import org.wyona.yarep.core.NodeStateException;
import org.wyona.yarep.core.NodeType;
import org.wyona.yarep.core.Property;
import org.wyona.yarep.core.PropertyType;
import org.wyona.yarep.core.Repository;
import org.wyona.yarep.core.RepositoryException;
import org.wyona.yarep.core.Revision;

/**
 * This class represents a repository node and implements some basic functionality which may be 
 * shared among different implementations.
 * A repository node may be either a collection ("directory") or a resource ("file").
 * If it is a resource, it has a binary default property, which may be accessed by using 
 * getInputStream() and getOutputStream().
 */
public abstract class AbstractNode implements Node {
    private static Category log = Category.getInstance(AbstractNode.class);

    protected Repository repository;
    protected String path;
    protected String name;
    protected String uuid;
    protected HashMap properties;
    protected LinkedHashMap revisions;
    
    // system properties:
    public static final String PROPERTY_TYPE = "yarep_type";
    //public static final String PROPERTY_CONTENT = "yarep_content";
    public static final String PROPERTY_SIZE = "yarep_size";
    public static final String PROPERTY_LAST_MODIFIED = "yarep_lastModifed";
    public static final String PROPERTY_MIME_TYPE = "yarep_mimeType";
    public static final String PROPERTY_ENCODING = "yarep_encoding";
    public static final String PROPERTY_IS_CHECKED_OUT = "yarep_isCheckedOut";
    public static final String PROPERTY_CHECKOUT_USER_ID = "yarep_checkoutUserID";
    public static final String PROPERTY_CHECKOUT_DATE = "yarep_checkoutDate";
    
    
    /**
     * Constructor
     * @throws RepositoryException
     */
    public AbstractNode(Repository repository, String path, String uuid) throws RepositoryException {
        this.repository = repository;
        this.path = path;
        this.name = PathUtil.getName(path);
        this.uuid = uuid;
    }
    
    /**
     * Gets the name of this node, i.e. the last part of the path.
     * @return name
     * @throws RepositoryException repository error
     */
    public String getName() throws RepositoryException {
        return this.name;
    }
    
    /**
     * Gets the parent node of this node.
     * @return parent node or null if this is the root node
     * @throws RepositoryException repository error
     */
    public Node getParent() throws RepositoryException {
        if (getPath().equals("") || getPath().equals("/")) return null;
        String parentPath = PathUtil.getParent(path);
        return this.repository.getNode(parentPath);
    }
    
    /**
     * Removes this node and all subnodes.
     * @throws RepositoryException repository error
     */
    public void remove() throws RepositoryException {
        // TODO: not implemented yet
        log.warn("Not implemented yet.");
    }

    /**
     * Gets the complete repository path of this node.
     * @return path
     * @throws RepositoryException repository error
     */
    public String getPath() throws RepositoryException {
        return this.path;
    }
    
    /**
     * Gets the UUID of this node.
     * @return uuid
     * @throws RepositoryException repository error
     */
    public String getUUID() throws RepositoryException {
        return this.uuid;
    }
    
    /**
     * Gets the type of this node (collection or resource).
     * @return type
     * @throws RepositoryException repository error
     */
    public int getType() throws RepositoryException {
        return NodeType.getType(getProperty(PROPERTY_TYPE).getString());
    }
    
    /**
     * Indicates whether this node is of type "resource".
     * @return true if type is resource
     * @throws RepositoryException repository error
     */
    public boolean isResource() throws RepositoryException {
        return getType() == NodeType.RESOURCE; 
    }
    
    /**
     * Indicates whether this node is of type "collection".
     * @return true if type is collection
     * @throws RepositoryException repository error
     */
    public boolean isCollection() throws RepositoryException {
        log.error("DEBUG: Node Type: " + getType() + ", " + getPath());
        return getType() == NodeType.COLLECTION; 
    }
    
    /**
     * Gets the child node with the given name. Must be a direct child.
     * @param name
     * @return
     * @throws NoSuchNodeException if no child node with this name exists.
     * @throws RepositoryException repository error
     */
    public Node getNode(String name) throws NoSuchNodeException, RepositoryException {
        String childPath = getPath() + "/" + name;
        return this.repository.getNode(childPath);
    }
    
    /**
     * Indicates whether this node has a child node with the given name.
     * @param name
     * @return
     * @throws RepositoryException repository error
     */
    public boolean hasNode(String name) throws RepositoryException {
        String childPath = getPath() + "/" + name;
        return this.repository.existsNode(childPath);
    }
    
    /**
     * Gets the property with the given name.
     * @param name
     * @return
     * @throws NoSuchPropertyException if the property does not exist.
     * @throws RepositoryException other error
     */
    public Property getProperty(String name) throws NoSuchPropertyException, RepositoryException {
        Property property = (Property)this.properties.get(name);
        if (property == null) throw new NoSuchPropertyException("Node " + getPath() + 
                " has no property named " + name);
        return property;
    }
    
    /**
     * Get all properties of this node
     * @return properties of this node or empty array if there are no properties.
     * @throws RepositoryException other error
     */
    public Property[] getProperties() throws RepositoryException {
        return (Property[])this.properties.values().toArray(new Property[this.properties.size()]);
    }
    
    /**
     * Indicates whether this node has a property with the given name.
     * @param name
     * @return
     * @throws RepositoryException repository error
     */
    public boolean hasProperty(String name) throws RepositoryException {
        return this.properties.containsKey(name);
    }
    
    //public boolean hasProperties() throws RepositoryException;
    
    /**
     * Sets a property of type boolean or creates it if it does not exist yet.
     * @param name
     * @param value
     * @return
     * @throws RepositoryException repository error
     */
    public Property setProperty(String name, boolean value) throws RepositoryException {
        Property property = new DefaultProperty(name, PropertyType.BOOLEAN, this);
        property.setValue(value);
        setProperty(property);
        return property;
    }
    
    /**
     * Sets a property of type date or creates it if it does not exist yet.
     * @param name
     * @param value
     * @return
     * @throws RepositoryException repository error
     */
    public Property setProperty(String name, Date value) throws RepositoryException {
        Property property = new DefaultProperty(name, PropertyType.DATE, this);
        property.setValue(value);
        setProperty(property);
        return property;
    }
    
    /**
     * Sets a property of type double or creates it if it does not exist yet.
     * @param name
     * @param value
     * @return
     * @throws RepositoryException repository error
     */
    public Property setProperty(String name, double value) throws RepositoryException {
        Property property = new DefaultProperty(name, PropertyType.DOUBLE, this);
        property.setValue(value);
        setProperty(property);
        return property;
    }
    
    //public Property setProperty(String name, InputStream value) throws RepositoryException;
    
    /**
     * Sets a property of type long or creates it if it does not exist yet.
     * @param name
     * @param value
     * @return
     * @throws RepositoryException repository error
     */
    public Property setProperty(String name, long value) throws RepositoryException {
        Property property = new DefaultProperty(name, PropertyType.LONG, this);
        property.setValue(value);
        setProperty(property);
        return property;
    }
    
    /**
     * Sets a property of type string or creates it if it does not exist yet.
     * @param name
     * @param value
     * @return
     * @throws RepositoryException repository error
     */
    public Property setProperty(String name, String value) throws RepositoryException {
        Property property = new DefaultProperty(name, PropertyType.STRING, this);
        property.setValue(value);
        setProperty(property);
        return property;
    }
    
    
    /**
     * Indicates whether this node is checked out.
     * @return
     * @throws RepositoryException repository error
     */
    public boolean isCheckedOut() throws RepositoryException {
        if (!hasProperty(PROPERTY_IS_CHECKED_OUT)) {
            return false;
        }
        return getProperty(PROPERTY_IS_CHECKED_OUT).getBoolean();
    }
    
    /**
     * Gets the userID which was supplied when calling checkout(userID).
     * @return
     * @throws NodeStateException if node is not checked out.
     * @throws RepositoryException
     */
    public String getCheckoutUserID() throws NodeStateException, RepositoryException {
        if (!isCheckedOut()) {
            throw new NodeStateException("Node is not checked out: " + getPath());
        }
        return getProperty(PROPERTY_CHECKOUT_USER_ID).getString();
    }
    
    /**
     * Gets the userID which was supplied when calling checkout(userID).
     * @return
     * @throws NodeStateException if node is not checked out.
     * @throws RepositoryException
     */
    public Date getCheckoutDate() throws NodeStateException, RepositoryException {
        if (!isCheckedOut()) {
            throw new NodeStateException("Node is not checked out: " + getPath());
        }
        return getProperty(PROPERTY_CHECKOUT_DATE).getDate();
    }
    
    /**
     * Gets all revisions of this node.
     * @return 
     * @throws RepositoryException
     */
    public Revision[] getRevisions() throws RepositoryException {
        Collection values =  this.revisions.values();
        return (Revision[])values.toArray(new Revision[values.size()]);
    }
    
    /**
     * Gets the revision with the given name.
     * @param revisionName
     * @return
     * @throws NoSuchRevisionException if the revision does not exist
     * @throws RepositoryException
     */
    public Revision getRevision(String revisionName) throws NoSuchRevisionException, RepositoryException {
        if (!this.revisions.containsKey(revisionName)) {
            throw new NoSuchRevisionException("Node " + getPath() + " has no revision with name: " + revisionName);
        }
        return (Revision)this.revisions.get(revisionName);
    }
    
    /**
     * @see org.wyona.yarep.core.Node#getRevisionByTag(java.lang.String)
     */
    public Revision getRevisionByTag(String tag) throws NoSuchRevisionException, RepositoryException {
        Iterator iter = this.revisions.values().iterator();
        
        while (iter.hasNext()) {
            Revision revision = (Revision)iter.next();
            if (revision.hasTag() && revision.getTag().equals(tag)) {
                return revision;
            }
        }
        // revision not found:
        throw new NoSuchRevisionException("Node " + getPath() + " has no revision with tag: " + tag);
    }
    
    /**
     * @see org.wyona.yarep.core.Node#hasRevisionWithTag(java.lang.String)
     */
    public boolean hasRevisionWithTag(String tag) throws RepositoryException {
        Iterator iter = this.revisions.values().iterator();
        
        while (iter.hasNext()) {
            Revision revision = (Revision)iter.next();
            if (revision.hasTag() && revision.getTag().equals(tag)) {
                return true;
            }
        }
        // revision not found:
        return false;
    }
    
    /**
     * Gets the last modified date in ms of this node.
     * Changing a property should update the last modified date.
     * @return
     * @throws RepositoryException
     */
    public long getLastModified() throws RepositoryException {
        return getProperty(PROPERTY_LAST_MODIFIED).getLong();
    }
    
    /**
     * Gets the size of the default property if this node is of type resource.
     * @return
     * @throws RepositoryException
     */
    public long getSize() throws RepositoryException {
        return getProperty(PROPERTY_SIZE).getLong();
    }
    
    /**
     * Gets the mimetype of the default property of this node if this node is of type resource.
     * @return
     * @throws RepositoryException
     */
    public String getMimeType() throws RepositoryException {
        return getProperty(PROPERTY_MIME_TYPE).getString();
    }
    
    /**
     * Sets the mimetype of the default property of this node if this node is of type resource.
     * @param mimeType
     * @throws RepositoryException
     */
    public void setMimeType(String mimeType) throws RepositoryException {
        setProperty(PROPERTY_MIME_TYPE, mimeType);
    }
    
    /**
     * Gets the encoding of the default property of this node if this node is of type resource.
     * @return
     * @throws RepositoryException
     */
    public String getEncoding() throws RepositoryException {
        return getProperty(PROPERTY_ENCODING).getString();
    }
    
    /**
     * Sets the encoding of the default property of this node if this node is of type resource.
     * @param encoding
     * @throws RepositoryException
     */
    public void setEncoding(String encoding) throws RepositoryException {
        setProperty(PROPERTY_ENCODING, encoding);
    }
        
}

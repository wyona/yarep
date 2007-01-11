package org.wyona.yarep.impl.repo.fs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.apache.log4j.Category;
import org.wyona.yarep.core.NoSuchNodeException;
import org.wyona.yarep.core.NoSuchPropertyException;
import org.wyona.yarep.core.NoSuchRevisionException;
import org.wyona.yarep.core.Node;
import org.wyona.yarep.core.NodeStateException;
import org.wyona.yarep.core.NodeType;
import org.wyona.yarep.core.Path;
import org.wyona.yarep.core.Property;
import org.wyona.yarep.core.PropertyType;
import org.wyona.yarep.core.RepositoryException;
import org.wyona.yarep.core.Revision;
import org.wyona.yarep.core.UID;
import org.wyona.yarep.impl.DefaultProperty;

/**
 * This class represents a repository node.
 * A repository node may be either a collection ("directory") or a resource ("file").
 * If it is a resource, it has a binary default property, which may be accessed by using 
 * getInputStream() and getOutputStream().
 */
public class FileSystemNode implements Node {
    private static Category log = Category.getInstance(FileSystemNode.class);

    protected FileSystemRepository repository;
    protected String path;
    protected String name;
    protected String uuid;
    protected Node parent;
    protected HashMap properties;
    protected LinkedHashMap revisions;
    protected File metaFile;
    
    // system properties:
    protected static final String PROPERTY_TYPE = "yarep_type";
    //protected static final String PROPERTY_CONTENT = "yarep_content";
    protected static final String PROPERTY_SIZE = "yarep_size";
    protected static final String PROPERTY_LAST_MODIFIED = "yarep_lastModifed";
    protected static final String PROPERTY_MIME_TYPE = "yarep_mimeType";
    protected static final String PROPERTY_ENCODING = "yarep_encoding";
    
    
    /**
     * Constructor
     * @throws RepositoryException
     */
    public FileSystemNode(FileSystemRepository repository, String path, String uuid) throws RepositoryException {
        this.repository = repository;
        this.path = path;
        this.name = "";
        if (path.indexOf("/") != -1) this.name = path.substring(path.lastIndexOf("/")+1);
        this.uuid = uuid;
        this.metaFile = new File(repository.getContentDir(), uuid + ".yarep" + File.separator + "meta");
        if (!metaFile.exists()) {
            createMetaFile();
        }
        readProperties();
    }
    
    protected void createMetaFile() throws RepositoryException {
        File metaDir = new File(repository.getContentDir(), uuid + ".yarep");
        log.debug("creating new meta file in dir: " + metaDir);
        if (!metaDir.exists()) {
            metaDir.mkdir();
        }
        this.properties = new HashMap();
        File contentFile = new File(repository.getContentDir(), uuid);
        if (contentFile.isDirectory()) {
            this.setProperty(PROPERTY_TYPE, NodeType.TYPENAME_COLLECTION);
        } else {
            this.setProperty(PROPERTY_TYPE, NodeType.TYPENAME_RESOURCE);
            this.setProperty(PROPERTY_SIZE, contentFile.length());
            this.setProperty(PROPERTY_LAST_MODIFIED, contentFile.lastModified());
        }
    }
    
    protected void readProperties() throws RepositoryException {
        try {
            log.debug("reading meta file: " + this.metaFile);
            this.properties = new HashMap();
            BufferedReader reader = new BufferedReader(new FileReader(this.metaFile));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                String name;
                String typeName;
                String value;
                try {
                    name = line.substring(0, line.indexOf("<")).trim();
                    typeName = line.substring(line.indexOf("<")+1, line.indexOf(">")).trim();
                    value = line.substring(line.indexOf(":")+1).trim();
                } catch (StringIndexOutOfBoundsException e) {
                    throw new RepositoryException("Error while parsing meta file: " + this.metaFile 
                            + " at line " + line);
                }
                Property property = new DefaultProperty(name, PropertyType.getType(typeName), this);
                property.setValueFromString(value);
                this.properties.put(name, property);
            }
            reader.close();
        } catch (IOException e) {
            throw new RepositoryException("Error while reading meta file: " + metaFile + ": " 
                    + e.getMessage());
        }
    }
    
    protected void saveProperties() throws RepositoryException {
        try {
            log.debug("writing meta file: " + this.metaFile);
            PrintWriter writer = new PrintWriter(new FileOutputStream(this.metaFile));
            Iterator iterator = this.properties.values().iterator();
            while (iterator.hasNext()) {
                Property property = (Property)iterator.next();
                writer.println(property.getName() + "<" + PropertyType.getTypeName(property.getType()) + 
                        ">:" + property.getValueAsString());
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            throw new RepositoryException("Error while reading meta file: " + metaFile + ": " 
                    + e.getMessage());
        }
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
        //String path = this.repository.getMap().getPath(this.uuid);
        if (getPath().equals("") || getPath().equals("/")) return null;
        String parentPath = (new Path(getPath())).getParent().toString();
        this.repository.getNode(parentPath);
        return this.parent;
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
        //String path = this.repository.getMap().getPath(this.uuid);
        //if (this.parent == null) return "";
        //String parentPath = this.parent.getPath();
        //return parentPath + "/" + this.name;
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
        return getType() == NodeType.COLLECTION; 
    }
    
    /**
     * Creates a new node and adds it as a child to this node.
     * @param name of the child node 
     * @return the new child node
     * @throws RepositoryException repository error
     */
    public Node addNode(String name, int type) throws RepositoryException {
        String newPath = getPath() + "/" + name;
        log.debug("adding node: " + newPath);
        if (this.repository.existsNode(newPath)) {
            throw new RepositoryException("Node exists already: " + newPath);
        }
        UID uid = this.repository.getMap().create(new Path(newPath));
        // create file:
        File file = new File(this.repository.getContentDir(), uid.toString());
        try {
            if (type == NodeType.COLLECTION) {
                file.mkdir();
            } else if (type == NodeType.RESOURCE) {
                file.createNewFile();
            } else {
                throw new RepositoryException("Unknown node type: " + type);
            }
            return this.repository.getNode(newPath);
        } catch (IOException e) {
            throw new RepositoryException("Could not access file " + file, e);
        }
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
     * Gets all child nodes.
     * @return child nodes or empty array if there are no child nodes.
     * @throws RepositoryException repository error
     */
    public Node[] getNodes() throws RepositoryException {
        Path[] childPaths = this.repository.getMap().getChildren(new Path(this.path));
        Node[] childNodes = new Node[childPaths.length];
        for (int i=0; i<childPaths.length; i++) {
            childNodes[i] = this.repository.getNode(childPaths.toString());
        }
        return childNodes;
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
    
    protected void setProperty(Property property) throws RepositoryException {
        this.properties.put(property.getName(), property);
        saveProperties();
    }

    //public Property getDefaultProperty() throws RepositoryException;
    
    /**
     * Gets an input stream of the binary default property.
     * Useful only for nodes of type resource.
     * @return
     * @throws RepositoryException repository error
     */
    public InputStream getInputStream() throws RepositoryException {
        File file = new File(this.repository.contentDir + File.separator + this.uuid);
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new RepositoryException(e.getMessage(), e);
        }
        //return getProperty(PROPERTY_CONTENT).getInputStream();
    }
    
    //public void setInputStream(InputStream inputStream) throws RepositoryException;
    
    /**
     * Gets an output stream of the binary default property.
     * Useful only for nodes of type resource.
     * @return
     * @throws RepositoryException repository error
     */
    public OutputStream getOutputStream() throws RepositoryException {
        File file = new File(this.repository.contentDir + File.separator + this.uuid);
        try {
            return new FileSystemOutputStream(this, file);
        } catch (FileNotFoundException e) {
            throw new RepositoryException(e.getMessage(), e);
        }
        //return getProperty(PROPERTY_CONTENT).getOutputStream();
    }
    
    /**
     * Checks in this node and creates a new revision.
     * @return
     * @throws NodeStateException if node is not in checked out state
     * @throws RepositoryException repository error
     */
    public Revision checkin() throws NodeStateException, RepositoryException {
        // TODO: not implemented yet
        log.warn("Not implemented yet.");
        return null;
    }
    
    /**
     * Checks out this node.
     * @throws NodeStateException if node is checked out by a different user
     * @throws RepositoryException repository error
     */
    public void checkout(String userID) throws NodeStateException, RepositoryException {
        // TODO: not implemented yet
        log.warn("Not implemented yet.");
    }
    
    /**
     * Indicates whether this node is checked out.
     * @return
     * @throws RepositoryException repository error
     */
    public boolean isCheckedOut() throws RepositoryException {
        // TODO: not implemented yet
        log.warn("Not implemented yet.");
        return false;
    }
    
    /**
     * Gets the userID which was supplied when calling checkout(userID).
     * @return
     * @throws NodeStateException if node is not checked out.
     * @throws RepositoryException
     */
    public String getCheckoutUserID() throws NodeStateException, RepositoryException {
        // TODO: not implemented yet
        log.warn("Not implemented yet.");
        return null;
    }
    
    /**
     * Gets all revisions of this node.
     * @return 
     * @throws RepositoryException
     */
    public Revision[] getRevisions() throws RepositoryException {
        // TODO: not implemented yet
        log.warn("Not implemented yet.");
        return null;
    }
    
    /**
     * Gets the revision with the given name.
     * @param revisionName
     * @return
     * @throws NoSuchRevisionException if the revision does not exist
     * @throws RepositoryException
     */
    public Revision getRevision(String revisionName) throws NoSuchRevisionException, RepositoryException {
        // TODO: not implemented yet
        log.warn("Not implemented yet.");
        return null;
    }
    
    /**
     * Gets the revision with the given name.
     * @param label
     * @return
     * @throws NoSuchRevisionException if the revision does not exist
     * @throws RepositoryException
     */
    public Revision getRevisionByLabel(String label) throws NoSuchRevisionException, RepositoryException {
        // TODO: not implemented yet
        log.warn("Not implemented yet.");
        return null;
    }
    
    /**
     * Restores the revision with the given name.
     * @param revisionName
     * @throws NoSuchRevisionException if the revision does not exist
     * @throws RepositoryException
     */
    public void restore(String revisionName) throws NoSuchRevisionException, RepositoryException {
        // TODO: not implemented yet
        log.warn("Not implemented yet.");
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
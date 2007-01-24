package org.wyona.yarep.impl;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

import org.apache.log4j.Category;
import org.wyona.yarep.core.Map;
import org.wyona.yarep.core.NoSuchRevisionException;
import org.wyona.yarep.core.Node;
import org.wyona.yarep.core.NodeStateException;
import org.wyona.yarep.core.NodeType;
import org.wyona.yarep.core.Path;
import org.wyona.yarep.core.Property;
import org.wyona.yarep.core.RepositoryException;
import org.wyona.yarep.core.Revision;
import org.wyona.yarep.core.Storage;
import org.wyona.yarep.core.UID;

/**
 * This is a dummy implementation for compatibility with the old repository structure.
 * Properties are not persisted, and versioning is not implemented.
 */
public class DummyNode extends AbstractNode {
    private static Category log = Category.getInstance(DummyNode.class);

    
    /**
     * Constructor
     * @throws RepositoryException
     */
    public DummyNode(DefaultRepository repository, String path, String uuid) throws RepositoryException {
        super(repository, path, uuid);
        setDummyProperties();
    }
    
    protected void setDummyProperties() throws RepositoryException {
        this.properties = new HashMap();
        Map map = ((DefaultRepository)this.repository).getMap();
        Storage storage = ((DefaultRepository)this.repository).getStorage();
        if (map.isCollection(new Path(this.path))) {
            this.setProperty(PROPERTY_TYPE, NodeType.TYPENAME_COLLECTION);
        } else {
            this.setProperty(PROPERTY_TYPE, NodeType.TYPENAME_RESOURCE);
            this.setProperty(PROPERTY_SIZE, storage.getSize(new UID(uuid), new Path(path)));
            this.setProperty(PROPERTY_LAST_MODIFIED, storage.getLastModified(new UID(uuid), new Path(path)));
        }
    }
    
    
    /**
     * Gets all child nodes.
     * @return child nodes or empty array if there are no child nodes.
     * @throws RepositoryException repository error
     */
    public Node[] getNodes() throws RepositoryException {
        
        Path[] childPaths = ((DefaultRepository)this.repository).getMap().getChildren(new Path(this.path));
        Node[] childNodes = new Node[childPaths.length];
        for (int i=0; i<childPaths.length; i++) {
            childNodes[i] = this.repository.getNode(childPaths[i].toString());
        }
        return childNodes;
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
        UID uid = ((DefaultRepository)this.repository).getMap().create(new Path(newPath));
        
        return new DummyNode((DefaultRepository)this.repository, newPath, uid.toString());
    }
    
    public void setProperty(Property property) throws RepositoryException {
        this.properties.put(property.getName(), property);
    }

    /**
     * Gets an input stream of the binary default property.
     * Useful only for nodes of type resource.
     * @return
     * @throws RepositoryException repository error
     */
    public InputStream getInputStream() throws RepositoryException {
        return this.repository.getInputStream(new Path(this.path));
    }
    
    /**
     * Gets an output stream of the binary default property.
     * Useful only for nodes of type resource.
     * @return
     * @throws RepositoryException repository error
     */
    public OutputStream getOutputStream() throws RepositoryException {
        return this.repository.getOutputStream(new Path(this.path));
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
     * Restores the revision with the given name.
     * @param revisionName
     * @throws NoSuchRevisionException if the revision does not exist
     * @throws RepositoryException
     */
    public void restore(String revisionName) throws NoSuchRevisionException, RepositoryException {
        // TODO: not implemented yet
        log.warn("Not implemented yet.");
    }
    
       
}
package org.wyona.yarep.impl;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;

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
        this.revisions = new LinkedHashMap();
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
     * @see org.wyona.yarep.core.Node#getNodes()
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
     * @see org.wyona.yarep.core.Node#addNode(java.lang.String, int)
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
    
    /**
     * @see org.wyona.yarep.core.Node#setProperty(org.wyona.yarep.core.Property)
     */
    public void setProperty(Property property) throws RepositoryException {
        this.properties.put(property.getName(), property);
    }

    /**
     * @see org.wyona.yarep.core.Node#getInputStream()
     */
    public InputStream getInputStream() throws RepositoryException {
        return this.repository.getInputStream(new Path(this.path));
    }
    
    /**
     * @see org.wyona.yarep.core.Node#getOutputStream()
     */
    public OutputStream getOutputStream() throws RepositoryException {
        return this.repository.getOutputStream(new Path(this.path));
    }
    
    /**
     * @see org.wyona.yarep.core.Node#checkin()
     */
    public Revision checkin() throws NodeStateException, RepositoryException {
        // TODO: not implemented yet
        log.warn("Not implemented yet.");
        return null;
    }
    
    /**
     * @see org.wyona.yarep.core.Node#checkin(java.lang.String)
     */
    public Revision checkin(String comment) throws NodeStateException, RepositoryException {
        // TODO: not implemented yet
        log.warn("Not implemented yet.");
        return null;
    }

    /**
     * @see org.wyona.yarep.core.Node#checkout(java.lang.String)
     */
    public void checkout(String userID) throws NodeStateException, RepositoryException {
        // TODO: not implemented yet
        log.warn("Not implemented yet.");
    }
    
    /**
     * @see org.wyona.yarep.core.Node#restore(java.lang.String)
     */
    public void restore(String revisionName) throws NoSuchRevisionException, RepositoryException {
        // TODO: not implemented yet
        log.warn("Not implemented yet.");
    }
    
       
}
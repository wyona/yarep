package org.wyona.yarep.impl.repo.vfs;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import org.apache.log4j.Logger;
import org.wyona.yarep.core.NoSuchNodeException;
import org.wyona.yarep.core.NoSuchRevisionException;
import org.wyona.yarep.core.Node;
import org.wyona.yarep.core.NodeStateException;
import org.wyona.yarep.core.Property;
import org.wyona.yarep.core.RepositoryException;
import org.wyona.yarep.core.Revision;

/**
 * A revision will only read the data from the filesystem if needed.
 */
public class VirtualFileSystemRevision extends VirtualFileSystemNode implements Revision {

    private static Logger log = Logger.getLogger(VirtualFileSystemRevision.class);
    
    public static final String PROPERTY_REVISION_CREATION_DATE = "yarep_revisionCreationDate";
    public static final String PROPERTY_REVISION_CREATOR = "yarep_revisionCreator";
    public static final String PROPERTY_REVISION_TAG = "yarep_revisionTag";
    public static final String PROPERTY_REVISION_COMMENT = "yarep_revisionComment";
    
    public static final String CONTENT_FILE_NAME = "content";

    protected VirtualFileSystemNode node;
    protected String revisionName;
    protected boolean isInitialized = false;
    
    /**
     * Constructor
     * @param node Node to which this revision belongs to
     * @param revisionName Name of this revision
     * @throws RepositoryException
     */
    public VirtualFileSystemRevision(VirtualFileSystemNode node, String revisionName) throws RepositoryException {
        super(node.getRepository(), node.getPath(), node.getUUID(), false);
        this.node = node;
        this.revisionName = revisionName;
        initContentAndMetaFile();
        // Defer the time consuming initialization until something is actually read from this revision (for performance reasons)
    }

    /**
     *
     */
    private void initContentAndMetaFile() throws RepositoryException {
        this.metaDir = node.getRevisionDir(this.revisionName);

        this.contentFile = node.getRevisionContentFile(this.revisionName);
        //log.debug("Revision content file: " + this.contentFile.getAbsolutePath());

        this.metaFile = node.getRevisionMetaFile(this.revisionName);;
        //log.debug("Revision meta file: " + this.metaFile.getAbsolutePath());
    }

    /**
     *
     */
    protected void init() throws RepositoryException {
        initContentAndMetaFile();

        if (log.isDebugEnabled()) {
            log.debug("VirtualFileSystemRevision: path=" + path + " uuid=" + uuid + " revisionName=" + revisionName);
            log.debug("contentDir=" + contentDir);
            log.debug("contentFile=" + contentFile);

            log.debug("metaDir=" + metaDir);
            log.debug("metaFile=" + metaFile);
        }

        if (!metaFile.exists()) {
            throw new RepositoryException("Meta file " + metaFile + " does not exist.");
        }
        readProperties();
        
        isInitialized = true;
    }


    /**
     * @see org.wyona.yarep.impl.repo.fs.VirtualFileSystemNode#addNode(java.lang.String, int)
     */
    public Node addNode(String name, int type) throws RepositoryException {
        throw new RepositoryException("cannot call this method on a revision");
    }
    
    /**
     * @see org.wyona.yarep.impl.repo.fs.VirtualFileSystemNode#checkin()
     */
    public Revision checkin() throws NodeStateException, RepositoryException {
        throw new RepositoryException("cannot call this method on a revision");
    }
    
    /**
     * @see org.wyona.yarep.impl.repo.fs.VirtualFileSystemNode#checkout(java.lang.String)
     */
    public void checkout(String userID) throws NodeStateException, RepositoryException {
        throw new RepositoryException("cannot call this method on a revision");
    }
    
    protected Revision createRevision() throws RepositoryException {
        throw new RepositoryException("cannot call this method on a revision");
    }
    
    /**
     * @see org.wyona.yarep.impl.repo.fs.VirtualFileSystemNode#restore(java.lang.String)
     */
    public void restore(String revisionName) throws NoSuchRevisionException, RepositoryException {
        throw new RepositoryException("cannot call this method on a revision");
    }
    
    /**
     * @see org.wyona.yarep.core.Revision#getCreationDate()
     */
    public Date getCreationDate() throws RepositoryException {
        Property property = getProperty(PROPERTY_REVISION_CREATION_DATE);
        if (property == null) {
            return null;    
        }
        return property.getDate();
    }

    /**
     * Sets the creation date of this revision.
     * @param date
     * @throws RepositoryException
     */
    public void setCreationDate(Date date) throws RepositoryException {
        setProperty(PROPERTY_REVISION_CREATION_DATE, date);
    }

    /**
     * @see org.wyona.yarep.core.Revision#getCreator()
     */
    public String getCreator() throws RepositoryException {
        Property property = getProperty(PROPERTY_REVISION_CREATOR);
        if (property == null) {
            return null;
        }
        return property.getString();
    }

    /**
     * Sets the creator of this revision.
     * @param creator user id
     * @throws RepositoryException
     */
    public void setCreator(String creator) throws RepositoryException {
        setProperty(PROPERTY_REVISION_CREATOR, creator);
    }

    /**
     * @see org.wyona.yarep.core.Revision#getComment()
     */
    public String getComment() throws RepositoryException {
        Property property = getProperty(PROPERTY_REVISION_COMMENT);
        if (property == null) {
            return null;
        }
        return property.getString();
    }

    /**
     * Sets the comment about this revision.
     * @param comment
     * @throws RepositoryException
     */
    public void setComment(String comment) throws RepositoryException {
        setProperty(PROPERTY_REVISION_COMMENT, comment);
    }

    /**
     * @see org.wyona.yarep.core.Revision#getTag()
     */
    public String getTag() throws RepositoryException {
        Property tag = getProperty(PROPERTY_REVISION_TAG);
        if (tag == null) return null;
        return tag.getString();
    }

    /**
     * @see org.wyona.yarep.core.Revision#setTag(java.lang.String)
     */
    public void setTag(String tag) throws RepositoryException {
        setProperty(PROPERTY_REVISION_TAG, tag);
    }    
       
    /**
     * @see org.wyona.yarep.core.Revision#hasTag()
     */
    public boolean hasTag() throws RepositoryException {
        return hasProperty(PROPERTY_REVISION_TAG);
    }    
       
    /**
     * @see org.wyona.yarep.core.Revision#getRevisionName()
     */
    public String getRevisionName() throws RepositoryException {
        return this.revisionName;
    }

    /**
     *
     */
    public String toString() {
        String s = "";
        try {
            s = s + getName() + ", " + getRevisionName() + ", " + getComment() + ", " + getCreator() + ", " + getCreationDate();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            s = s + e.getMessage();
        }
        return s;
    }

    /**
     * @see org.wyona.yarep.core.Node#delete()
     */
    @Override
    public void delete() throws RepositoryException {
        if (!isInitialized) {
            init();
        }

        // INFO: Delete from index first, before deleting revision itself!
        DateIndexerSearcher dis = node.getDateIndexerSearcher();
        try {
            dis.deleteRevision(revisionName);
        } catch(Exception e) {
            log.error(e, e);
        }

        super.delete();
        deleteEmptyDirectories(metaDir);

        if (node.hasProperty(VirtualFileSystemNode.PROPERTY_TOTAL_NUMBER_OF_REVISIONS)) {
            long currentTotal = node.getProperty(VirtualFileSystemNode.PROPERTY_TOTAL_NUMBER_OF_REVISIONS).getLong();
            node.setProperty(VirtualFileSystemNode.PROPERTY_TOTAL_NUMBER_OF_REVISIONS, currentTotal - 1);
        }
    }

    /**
     * Delete empty directories recursively upwards
     * @param dir Directory which will be deleted if it is empty, e.g. '/Users/michaelwechner/src/yanel/src/realms/yanel-website/data-repo/yarep-meta/en/about.html.yarep/revisions/11/71/84/25/41/025'
     */
    private void deleteEmptyDirectories(File dir) {
        if (dir.equals(node.getRevisionsBaseDir())) {
            return;
        }
        if (dir.isDirectory()) {
            if (isEmpty(dir)) {
                File parentDir = dir.getParentFile();
                dir.delete();
                deleteEmptyDirectories(parentDir);
            }
        } else {
            log.warn("No such directory: " + dir.getAbsolutePath());
            File parentDir = dir.getParentFile();
            if (parentDir != null) {
                deleteEmptyDirectories(parentDir);
            } else {
                return;
            }
        }
    }

    /**
     * Check whether a directory is empty
     * @param dir Directory to be checked
     * @return true if directory is empty and false otherwise
     */
    private boolean isEmpty(File dir) {
        String[] filesAndDirs = dir.list();
        if (filesAndDirs !=  null && filesAndDirs.length > 0) {
            log.warn("DEBUG: Directory '" + dir.getAbsolutePath() + "' is NOT empty.");
            return false;
        }
        log.warn("DEBUG: Directory '" + dir.getAbsolutePath() + "' is empty.");
        return true;
    }

    public InputStream getInputStream() throws RepositoryException {
        if (!isInitialized) {
            init();
        }
        return super.getInputStream();
    }

    public long getLastModified() throws RepositoryException {
        if (!isInitialized) {
            init();
        }
        return super.getLastModified();
    }

    public Node getNode(String name) throws NoSuchNodeException, RepositoryException {
        if (!isInitialized) {
            init();
        }
        return super.getNode(name);
    }

    public Node[] getNodes() throws RepositoryException {
        if (!isInitialized) {
            init();
        }
        return super.getNodes();
    }

    public OutputStream getOutputStream() throws RepositoryException {
        if (!isInitialized) {
            init();
        }
        return super.getOutputStream();
    }

    public Property[] getProperties() throws RepositoryException {
        if (!isInitialized) {
            init();
        }
        return super.getProperties();
    }

    /**
     * @see org.wyona.yarep.core.Node#getProperty(String)
     */
    public Property getProperty(String name) throws RepositoryException {
        if (!isInitialized) {
            init();
        }
        return super.getProperty(name);
    }

    public long getSize() throws RepositoryException {
        if (!isInitialized) {
            init();
        }
        return super.getSize();
    }

    public boolean hasProperty(String name) throws RepositoryException {
        if (!isInitialized) {
            init();
        }
        return super.hasProperty(name);
    }

    public void removeProperty(String name) throws RepositoryException {
        if (!isInitialized) {
            init();
        }
        super.removeProperty(name);
    }

    public Property setProperty(String name, boolean value) throws RepositoryException {
        if (!isInitialized) {
            init();
        }
        return super.setProperty(name, value);
    }

    /**
     * @see org.wyona.yarep.core.Node#setProperty(String, Date)
     */
    public Property setProperty(String name, Date value) throws RepositoryException {
        if (!isInitialized) {
            init();
        }
        return super.setProperty(name, value);
    }

    public Property setProperty(String name, double value) throws RepositoryException {
        if (!isInitialized) {
            init();
        }
        return super.setProperty(name, value);
    }

    public Property setProperty(String name, long value) throws RepositoryException {
        if (!isInitialized) {
            init();
        }
        return super.setProperty(name, value);
    }

    /**
     * @see org.wyona.yarep.core.Node#setProperty(String, String)
     */
    public Property setProperty(String name, String value) throws RepositoryException {
        if (!isInitialized) {
            init();
        }
        //log.debug("Set property: " + name + ", " + value);
        return super.setProperty(name, value);
    }

    public void setProperty(Property property) throws RepositoryException {
        if (!isInitialized) {
            init();
        }
        super.setProperty(property);
    }
}

package org.wyona.yarep.impl.repo.fs;

import java.io.File;
import java.util.Date;

import org.apache.log4j.Category;
import org.wyona.yarep.core.NoSuchRevisionException;
import org.wyona.yarep.core.Node;
import org.wyona.yarep.core.NodeStateException;
import org.wyona.yarep.core.RepositoryException;
import org.wyona.yarep.core.Revision;

/**
 */
public class FileSystemRevision extends FileSystemNode implements Revision {

    private static Category log = Category.getInstance(FileSystemRevision.class);
    
    public static final String PROPERTY_REVISION_CREATION_DATE = "yarep_revisionCreationDate";
    public static final String PROPERTY_REVISION_CREATOR = "yarep_revisionCreator";
    public static final String PROPERTY_REVISION_TAG = "yarep_revisionTag";
    public static final String PROPERTY_REVISION_COMMENT = "yarep_revisionComment";

    public static final String CONTENT_FILE_NAME = "content";

    protected Node node;
    protected String revisionName;
    
    /**
     * Constructor
     * @throws RepositoryException
     */
    public FileSystemRevision(FileSystemNode node, String revisionName) throws RepositoryException {
        super(node.getRepository(), node.getPath(), node.getUUID(), false);

        this.revisionName = revisionName;
        this.contentDir = new File(((FileSystemRepository)repository).getContentDir(), this.uuid + META_DIR_SUFFIX + File.separator + REVISIONS_BASE_DIR + File.separator + this.revisionName);
        this.contentFile = new File(this.contentDir, CONTENT_FILE_NAME);
        this.metaDir = this.contentDir;
        this.metaFile = new File(this.metaDir, META_FILE_NAME);
    
        if (log.isDebugEnabled()) {
            log.debug("FileSystemRevision: path=" + path + " uuid=" + uuid + " revisionName=" + revisionName);
            log.debug("contentDir=" + contentDir);
            log.debug("contentFile=" + contentFile);
            log.debug("metaDir=" + metaDir);
            log.debug("metaFile=" + metaFile);
        }

        if (!metaFile.exists()) {
            throw new RepositoryException("Meta file " + metaFile + " does not exist.");
        }
        readProperties();
    }
    
    /**
     * Creates a new node and adds it as a child to this node.
     * @param name of the child node 
     * @return the new child node
     * @throws RepositoryException repository error
     */
    public Node addNode(String name, int type) throws RepositoryException {
        throw new RepositoryException("cannot call this method on a revision");
    }
    
    /**
     * Checks in this node and creates a new revision.
     * @return
     * @throws NodeStateException if node is not in checked out state
     * @throws RepositoryException repository error
     */
    public Revision checkin() throws NodeStateException, RepositoryException {
        throw new RepositoryException("cannot call this method on a revision");
    }
    
    /**
     * Checks out this node.
     * @throws NodeStateException if node is checked out by a different user
     * @throws RepositoryException repository error
     */
    public void checkout(String userID) throws NodeStateException, RepositoryException {
        throw new RepositoryException("cannot call this method on a revision");
    }
    
    protected Revision createRevision() throws RepositoryException {
        throw new RepositoryException("cannot call this method on a revision");
    }
    
    /**
     * Restores the revision with the given name.
     * @param revisionName
     * @throws NoSuchRevisionException if the revision does not exist
     * @throws RepositoryException
     */
    public void restore(String revisionName) throws NoSuchRevisionException, RepositoryException {
        throw new RepositoryException("cannot call this method on a revision");
    }
    
    public Date getCreationDate() throws RepositoryException {
        return getProperty(PROPERTY_REVISION_CREATION_DATE).getDate();
    }

    public void setCreationDate(Date date) throws RepositoryException {
        setProperty(PROPERTY_REVISION_CREATION_DATE, date);
    }

    public String getCreator() throws RepositoryException {
        return getProperty(PROPERTY_REVISION_CREATOR).getString();
    }

    public void setCreator(String creator) throws RepositoryException {
        setProperty(PROPERTY_REVISION_CREATOR, creator);
    }

    public String getComment() throws RepositoryException {
        return getProperty(PROPERTY_REVISION_COMMENT).getString();
    }

    public void setComment(String comment) throws RepositoryException {
        setProperty(PROPERTY_REVISION_COMMENT, comment);
    }

    public String getTag() throws RepositoryException {
        return getProperty(PROPERTY_REVISION_TAG).getString();
    }

    public void setTag(String tag) throws RepositoryException {
        setProperty(PROPERTY_REVISION_TAG, tag);
    }    
       
    public boolean hasTag() throws RepositoryException {
        return hasProperty(PROPERTY_REVISION_TAG);
    }    
       
    public String getRevisionName() throws RepositoryException {
        return this.revisionName;
    }

}
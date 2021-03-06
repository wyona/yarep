package org.wyona.yarep.impl.repo.fs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Category;
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
import org.wyona.yarep.impl.AbstractNode;
import org.wyona.yarep.impl.DefaultProperty;
import org.wyona.yarep.impl.repo.fs.FileSystemRepository;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexWriter;

/**
 * This class represents a repository node.
 * A repository node may be either a collection ("directory") or a resource ("file").
 */
public class FileSystemNode extends AbstractNode {
    private static Category log = Category.getInstance(FileSystemNode.class);

    protected static final String META_FILE_NAME = "meta";
    protected static final String REVISIONS_BASE_DIR = "revisions";
    protected static final String META_DIR_SUFFIX = ".yarep";
    
    protected File contentDir;
    protected File contentFile;
    protected File metaDir;
    protected File metaFile;
    
    protected RevisionDirectoryFilter revisionDirectoryFilter = new RevisionDirectoryFilter();
    
    
    /**
     * Constructor
     * @throws RepositoryException
     */
    public FileSystemNode(FileSystemRepository repository, String path, String uuid) throws RepositoryException {
        super(repository, path, uuid);
        init();
    }
    
    /**
     * Constructor
     * @throws RepositoryException
     */
    protected FileSystemNode(FileSystemRepository repository, String path, String uuid, boolean doInit) throws RepositoryException {
        super(repository, path, uuid);
        
        if (doInit) {
            init();
        }
    }
    
    /**
     * Init file system impl node
     */
    protected void init() throws RepositoryException {
        
        this.contentDir = getRepository().getContentDir();
        this.contentFile = determineContentFile(this.uuid);
        this.metaDir = determineMetaDir(this.uuid);
        this.metaFile = determineMetaFile(this.uuid);
        
        if (log.isDebugEnabled()) {
            log.debug("FileSystemNode: path=" + path + " uuid=" + uuid);
            log.debug("contentDir=" + contentDir);
            log.debug("contentFile=" + contentFile);
            log.debug("metaDir=" + metaDir);
            log.debug("metaFile=" + metaFile);
        }
        
        if (!metaFile.exists()) {
            createMetaFile();
        }
        readProperties();
        readRevisions();
    }
    
    protected File determineContentFile(String uuid) {
        return new File(this.contentDir, uuid);
    }
    
    /**
     * Determine yarep meta directory
     */
    protected File determineMetaDir(String uuid) {
        if (getRepository().getYarepMetaDir() != null) {
            return new File(getRepository().getYarepMetaDir(), uuid + META_DIR_SUFFIX);
        } else {
            return new File(this.contentDir, uuid + META_DIR_SUFFIX);
        }
    }
    
    /**
     * Determine yarep meta file of this node
     */
    protected File determineMetaFile(String uuid) {
        return new File(this.metaDir, META_FILE_NAME);
    }
    
    /**
     *
     */
    protected void createMetaFile() throws RepositoryException {
        if (!metaDir.exists()) {
            metaDir.mkdirs();
            log.warn("Creating new meta directory: " + metaDir);
        }
        log.debug("Set node type property ...");
        this.properties = new HashMap();
        if (this.contentFile.isDirectory()) {
            this.setProperty(PROPERTY_TYPE, NodeType.TYPENAME_COLLECTION);
        } else {
            this.setProperty(PROPERTY_TYPE, NodeType.TYPENAME_RESOURCE);
            //this.setProperty(PROPERTY_SIZE, this.contentFile.length());
            //this.setProperty(PROPERTY_LAST_MODIFIED, this.contentFile.lastModified());
        }
    }
    
    /**
     * Read properties from meta file
     */
    protected void readProperties() throws RepositoryException {
        try {
            log.debug("Reading meta file: " + this.metaFile);
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
    
    /**
     * FIXME: this implementation does not work correctly when a string property contains a line-break. 
     * @throws RepositoryException
     */
    protected void saveProperties() throws RepositoryException {
        try {
            // get repository
            FileSystemRepository fsRepo = getRepository();
            
            // the lucene index location
            File propertiesSearchIndexFile =  fsRepo.getPropertiesSearchIndexFile();

            // get lucene index writer, create the index if it does not exist yet
            IndexWriter indexWriter = null;
            if (propertiesSearchIndexFile != null) {
                if (propertiesSearchIndexFile.isDirectory()) {
                    indexWriter = new IndexWriter(propertiesSearchIndexFile.getAbsolutePath(), fsRepo.getWhitespaceAnalyzer(), false);
                } else {
                    indexWriter = new IndexWriter(propertiesSearchIndexFile.getAbsolutePath(), fsRepo.getWhitespaceAnalyzer(), true);
                }
            } else {
                log.warn("Directory of search index for properties is not set!");
	    }
            
            // prepare the lucene document
            Document document = new Document();
            
            log.debug("writing meta file: " + this.metaFile);
            PrintWriter writer = new PrintWriter(new FileOutputStream(this.metaFile));
            Iterator iterator = this.properties.values().iterator();
            while (iterator.hasNext()) {
                Property property = (Property)iterator.next();
                writer.println(property.getName() + "<" + PropertyType.getTypeName(property.getType()) + 
                        ">:" + property.getValueAsString());
                
                // add the property to the lucene document
                // TODO: write typed property value to index. possible?
                if (property.getValueAsString() != null) {
                    document.add(new Field(property.getName(), property.getValueAsString(), Field.Store.YES, Field.Index.UN_TOKENIZED));
                } else {
                    log.warn("Property '" + property.getName() + "' has null as value and hence will not be indexed (path: " + this.getPath() + ")!");
                }
            }
            writer.flush();
            writer.close();
            
            // store the lucene document
            document.add(new Field("_PATH", this.getPath(), Field.Store.YES, Field.Index.UN_TOKENIZED));
            if (indexWriter != null) {
                indexWriter.updateDocument(new org.apache.lucene.index.Term("_PATH", this.getPath()), document);
                indexWriter.close();
            } else {
                log.warn("Index writer for properties search is null!");
            }
            
        } catch (IOException e) {
            throw new RepositoryException("Error while reading meta file: " + metaFile + ": " 
                    + e.getMessage());
        }
    }
    
    /**
     * @see org.wyona.yarep.core.Node#getNodes()
     */
    public Node[] getNodes() throws RepositoryException {
        Path[] childPaths = getRepository().getMap().getChildren(new Path(this.path));
        FileSystemRepository repo = (FileSystemRepository) this.repository;

        Vector childNodes = new Vector();
        for (int i = 0; i < childPaths.length; i++) {
            childNodes.addElement(repo.getNode(childPaths[i].toString()));
        }

        // Also add fallback nodes if fallback is enabled
        if (repo.isFallbackEnabled()) {
            log.warn("Fallback is enabled for repository '" + repo.getName() + "' and hence children will also retrieved from storage without being listed within map!");
            File contentDir = repo.contentDir;
            String path = contentDir.getPath() + this.path;
            File currentDir = new File(path);
            
            if (currentDir.isDirectory()) {
                File[] files = null;
                files = currentDir.listFiles(new YarepMetaDataDirectoryFilter());
                for (int i = 0; i < files.length; i++) {
                    String fpath = this.path + "/" + files[i].getName();
                    boolean alreadyExists = false;
                    for (int k = 0; k < childPaths.length; k++) {
                        if (files[i].getName().equals(childPaths[k].getName())) {
                            alreadyExists = true;
                            break;
                        }
                    }
                    if (!alreadyExists) {
                            log.info("No UID! Fallback to : " + fpath);
                            String uuid2 = new UID(fpath).toString();
                            childNodes.addElement(new FileSystemNode(repo, fpath, uuid2));
                    }
                }
            }
        }
        Node[] children = new Node[childNodes.size()];
        for (int i = 0; i < children.length; i++) {
            children[i] = (Node) childNodes.elementAt(i);
        }
        return children;
    }
    
    /**
     * @see org.wyona.yarep.core.Node#addNode(java.lang.String, int)
     */
    public Node addNode(String name, int type) throws RepositoryException {
        String newPath = getPath() + "/" + name;
        log.debug("Adding node: " + newPath);
        if (this.repository.existsNode(newPath)) {
            log.warn("Node already exists: " + newPath);
            throw new RepositoryException("Node exists already: " + newPath);
        }
        UID uid = getRepository().getMap().create(new Path(newPath), type);
        // create file:
        File file = determineContentFile(uid.toString());
        try {
            if (type == NodeType.COLLECTION) {
                file.mkdirs();
                log.warn("Directory created: " + file);
            } else if (type == NodeType.RESOURCE) {
                File parentFile = file.getParentFile(); 
                if (!parentFile.exists()) {
                    parentFile.mkdirs();
                }
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
     * @see org.wyona.yarep.core.Node#removeProperty(java.lang.String)
     */
    public void removeProperty(String name) throws RepositoryException {
        this.properties.remove(name);
        saveProperties();
    }
    
    /**
     * @see org.wyona.yarep.core.Node#setProperty(org.wyona.yarep.core.Property)
     */
    public void setProperty(Property property) throws RepositoryException {
        this.properties.put(property.getName(), property);
        saveProperties();
    }

    /**
     * @see org.wyona.yarep.core.Node#getInputStream()
     */
    public InputStream getInputStream() throws RepositoryException {
        try {
            return new FileInputStream(this.contentFile);
        } catch (FileNotFoundException e) {
            throw new RepositoryException(e.getMessage(), e);
        }
        //return getProperty(PROPERTY_CONTENT).getInputStream();
    }
    
    /**
     * @see org.wyona.yarep.core.Node#getOutputStream()
     */
    public OutputStream getOutputStream() throws RepositoryException {
        try {
            //return new FileOutputStream(this.contentFile);
            return new FileSystemOutputStream(this, this.contentFile);
        } catch (FileNotFoundException e) {
            throw new RepositoryException(e.getMessage(), e);
        }
        //return getProperty(PROPERTY_CONTENT).getOutputStream();
    }
    
    /**
     * @see org.wyona.yarep.core.Node#checkin()
     */
    public Revision checkin() throws NodeStateException, RepositoryException {
        return checkin("");
    }
    
    /**
     * @see org.wyona.yarep.core.Node#checkin()
     */
    public Revision checkin(String comment) throws NodeStateException, RepositoryException {
        if (!isCheckedOut()) {
            throw new NodeStateException("Node " + path + " is not checked out.");
        }
        Revision revision = createRevision(comment);
        
        setProperty(PROPERTY_IS_CHECKED_OUT, false);
        setProperty(PROPERTY_CHECKIN_DATE, new Date());
        
        return revision;
    }
    
    public void cancelCheckout() throws NodeStateException, RepositoryException {
        if (!isCheckedOut()) {
            throw new NodeStateException("Node " + path + " is not checked out.");
        }
        
        setProperty(PROPERTY_IS_CHECKED_OUT, false);
        setProperty(PROPERTY_CHECKIN_DATE, new Date());
    }
    
    /**
     * @see org.wyona.yarep.core.Node#checkout(java.lang.String)
     */
    public void checkout(String userID) throws NodeStateException, RepositoryException {
        // TODO: this should be somehow synchronized
        if (isCheckedOut()) {
            throw new NodeStateException("Node " + path + " is already checked out by: " + getCheckoutUserID());
        }
        
        setProperty(PROPERTY_IS_CHECKED_OUT, true);
        setProperty(PROPERTY_CHECKOUT_USER_ID, userID);
        setProperty(PROPERTY_CHECKOUT_DATE, new Date());

        /*if (getRevisions().length == 0) {
            // create a backup revision
            createRevision("initial revision");
        }*/
    }
    
    protected Revision createRevision(String comment) throws RepositoryException {
        try {
            File revisionsBaseDir = new File(this.metaDir, REVISIONS_BASE_DIR);
            String revisionName = String.valueOf(System.currentTimeMillis());
            File revisionDir = new File(revisionsBaseDir, revisionName);
            
            File destContentFile = new File(revisionDir, FileSystemRevision.CONTENT_FILE_NAME);
            FileUtils.copyFile(this.contentFile, destContentFile);
        
            File destMetaFile = new File(revisionDir, META_FILE_NAME);
            FileUtils.copyFile(this.metaFile, destMetaFile);
        
            Revision revision = new FileSystemRevision(this, revisionName);
            revision.setProperty(PROPERTY_IS_CHECKED_OUT, false);
            ((FileSystemRevision)revision).setCreationDate(new Date());
            ((FileSystemRevision)revision).setCreator(getCheckoutUserID());
            ((FileSystemRevision)revision).setComment(comment);
            this.revisions.put(revisionName, revision);
            return revision;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new RepositoryException(e.getMessage(), e);
        }
    }
    
    protected void readRevisions() throws RepositoryException {
        File revisionsBaseDir = new File(this.metaDir, REVISIONS_BASE_DIR);
        
        File[] revisionDirs = revisionsBaseDir.listFiles(this.revisionDirectoryFilter);
        
        this.revisions = new LinkedHashMap();
        
        if (revisionDirs != null) {
            Arrays.sort(revisionDirs);
            for (int i=0; i<revisionDirs.length; i++) {
                String revisionName = revisionDirs[i].getName();
                Revision revision = new FileSystemRevision(this, revisionName);
                this.revisions.put(revisionName, revision);
            }
        }
    }
    
    /**
     * @see org.wyona.yarep.core.Node#restore(java.lang.String)
     */
    public void restore(String revisionName) throws NoSuchRevisionException, RepositoryException {
        try {
            File revisionsBaseDir = new File(this.metaDir, REVISIONS_BASE_DIR);
            File revisionDir = new File(revisionsBaseDir, revisionName);
            
            File srcContentFile = new File(revisionDir, FileSystemRevision.CONTENT_FILE_NAME);
            FileUtils.copyFile(srcContentFile, this.contentFile);
        
            File srcMetaFile = new File(revisionDir, META_FILE_NAME);
            FileUtils.copyFile(srcMetaFile, this.metaFile);
            
            setProperty(AbstractNode.PROPERTY_LAST_MODIFIED, this.contentFile.lastModified());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new RepositoryException(e.getMessage(), e);
        }
    }
    
    protected class RevisionDirectoryFilter implements FileFilter {
        public RevisionDirectoryFilter() {
        }
        public boolean accept(File pathname) {
            if (pathname.getName().matches("[0-9]+") && pathname.isDirectory()) {
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * Filter in order to filter ".yarep" nodes
     */
    protected class YarepMetaDataDirectoryFilter implements FileFilter {
        public YarepMetaDataDirectoryFilter() {
        }
        public boolean accept(File pathname) {
            if (pathname.getName().endsWith(META_DIR_SUFFIX) && pathname.isDirectory()) {
                return false;
            } else {
                return true;
            }
        }
    }
    
    /**
     * Changing a property should update the last modified date.
     * FIXME: This implementation does not change the last modified date if a property changes.
     * @see org.wyona.yarep.impl.AbstractNode#getLastModified()
     */
    public long getLastModified() throws RepositoryException {
        return this.contentFile.lastModified();
    }
    
    /**
     * @see org.wyona.yarep.impl.AbstractNode#getSize()
     */
    public long getSize() throws RepositoryException {
        return this.contentFile.length();
    }
    

    protected FileSystemRepository getRepository() {
        return (FileSystemRepository)this.repository;
    }

    /**
     * @see org.wyona.yarep.core.Node#delete()
     */
    public void delete() throws RepositoryException {
        deleteRec(this);
    }
       
    protected void deleteRec(Node node) throws RepositoryException {
        Node[] children = node.getNodes();
        for (int i=0; i<children.length; i++) {
            deleteRec(children[i]);
        }
        boolean success = getRepository().getMap().delete(new Path(getPath()));
        try {
            if (this.contentFile.isDirectory()) {
                FileUtils.deleteDirectory(this.contentFile);
            } else {
                this.contentFile.delete();
            }
            FileUtils.deleteDirectory(this.metaDir);
        } catch (IOException e) {
            throw new RepositoryException("Could not delete node: " + node.getPath() + ": " + 
                    e.toString(), e);
        }
    }
       
}

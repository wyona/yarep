package org.wyona.yarep.impl.repo.fs;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.log4j.Category;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.IndexSearcher;

import org.wyona.commons.io.FileUtil;
import org.wyona.yarep.core.Map;
import org.wyona.yarep.core.NoSuchNodeException;
import org.wyona.yarep.core.Node;
import org.wyona.yarep.core.Path;
import org.wyona.yarep.core.Repository;
import org.wyona.yarep.core.RepositoryException;
import org.wyona.yarep.core.Revision;
import org.wyona.yarep.core.Storage;
import org.wyona.yarep.core.UID;

/**
 * Node based file system repository.
 * A node of type resource is stored as a file.
 * A node of type collection is stored as a directory.
 * Each resource has a myresource.yarep directory which contains:
 * <ul>
 * <li>A meta file containing the properties</li>
 * <li>A revisions directory containing the revisions</li>
 * </ul>
 * This directory and the meta file will be created automatically when a node is
 * accessed which does not have such a .yarep directory yet.
 */
public class FileSystemRepository implements Repository {

    private static Category log = Category.getInstance(FileSystemRepository.class);

    protected String id;
    protected File configFile;

    protected String name;

    protected Map map;
    protected Storage storage;

    private boolean fallback = false;

    // Search and index
    private File searchIndexFile = null;
    private Analyzer analyzer = null;
    private String PROPERTIES_INDEX_DIR = "properties";

    /**
     *
     */
    public FileSystemRepository() {
    }
    
    /**
     *
     */
    public FileSystemRepository(String id, File configFile) throws RepositoryException {
        setID(id);
        readConfiguration(configFile);
    }

    /**
     * Read respectively load repository configuration
     */
    public void readConfiguration(File configFile) throws RepositoryException {
        this.configFile = configFile;
        DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();
        Configuration config;

        try {
            config = builder.buildFromFile(configFile);

            name = config.getChild("name", false).getValue();

            Configuration pathConfig = config.getChild("paths", false);

            fallback = pathConfig.getAttributeAsBoolean("fallback", false);
            String pathsClassname = pathConfig.getAttribute("class", null);
            if (pathsClassname != null) {
                log.debug(pathsClassname);
                Class pathsClass = Class.forName(pathsClassname);
                map = (Map) pathsClass.newInstance();
            } else {
                map = (Map) Class.forName("org.wyona.yarep.impl.DefaultMapImpl").newInstance();
                //map = new org.wyona.yarep.impl.DefaultMapImpl();
            }
            map.readConfig(pathConfig, configFile);

            this.contentDir = new File(config.getChild("content", false).getAttribute("src"));
            
            if (!this.contentDir.isAbsolute()) {
                this.contentDir = FileUtil.file(configFile.getParent(), this.contentDir.toString());
            }
            log.info("Content dir: " + this.contentDir);

            Configuration metaDirConfig = config.getChild("meta", false);
            if (metaDirConfig != null) {
                this.metaDir = new File(metaDirConfig.getAttribute("src"));

                if (!this.metaDir.isAbsolute()) {
                    this.metaDir = FileUtil.file(configFile.getParent(), this.metaDir.toString());
                }
                log.info("Meta dir: " + this.metaDir);
            }

            Configuration searchIndexConfig = config.getChild("search-index", false);
            if (searchIndexConfig != null) {
                searchIndexFile = new File(searchIndexConfig.getAttribute("src", "index"));
            
                if (!searchIndexFile.isAbsolute()) {
                    searchIndexFile = FileUtil.file(configFile.getParent(), searchIndexFile.toString());
                }

		analyzer = new StandardAnalyzer();

                // Create a lucene search index (for fulltext) if it doesn't exist yet
                if (!searchIndexFile.isDirectory()) {
                    IndexWriter indexWriter = new IndexWriter(searchIndexFile.getAbsolutePath(), getAnalyzer(), true);
                    indexWriter.close();
                }
                // TODO: Create properties index dir subdirectory in order to save the lucene index for searching on properties
                //searchIndexFile.mkdir(PROPERTIES_INDEX_DIR);
            }

        } catch (Exception e) {
            log.error(e.toString());
            throw new RepositoryException("Could not read repository configuration: " 
                    + e.getMessage(), e);
        }
    }

    /**
    *
    */
   public String toString() {
       return "Repository: ID = " + id + ", Configuration-File = " + configFile + ", Name = " + name;
   }

   /**
    * Get repository ID
    */
   public String getID() {
       return id;
   }

   /**
    * Set repository ID
    */
   public void setID(String id) {
       this.id = id;
   }

   /**
    * Get repository name
    */
   public String getName() {
       return name;
   }

   /**
    * Get repository configuration file
    */
   public File getConfigFile() {
       return configFile;
   }
   
    public void addSymbolicLink(Path target, Path link) throws RepositoryException {
        log.warn("Not implemented.");
    }

    public boolean delete(Path path) throws RepositoryException {
        getNode(path.toString()).delete();
        return true;
    }

    /**
     * @return true if node has been deleted, otherwise false
     */
    public boolean delete(Path path, boolean recursive) throws RepositoryException {
        log.warn("Not implemented yet!");
        if (recursive) throw new RepositoryException("Not implemented yet");
        return delete(path);
    }

    public boolean exists(Path path) throws RepositoryException {
        return existsNode(path.toString());
    }

    /**
     * Get paths of children
     */
    public Path[] getChildren(Path path) throws RepositoryException {
        Node node = getNode(path.toString());
        Node[] childNodes = node.getNodes();
        Path[] childPaths = new Path[childNodes.length];
        for (int i=0; i<childNodes.length; i++) {
            childPaths[i] = new Path(childNodes[i].getPath());
        }
        return childPaths;
    }

    public void getContentLength(Path path) throws RepositoryException {
        log.warn("Not implemented.");
    }

    public InputStream getInputStream(Path path) throws RepositoryException {
        return getNode(path.toString()).getInputStream();
    }

    public long getLastModified(Path path) throws RepositoryException {
        return getNode(path.toString()).getLastModified();
    }

    public OutputStream getOutputStream(Path path) throws RepositoryException {
        return getNode(path.toString()).getOutputStream();
    }

    public Reader getReader(Path path) throws RepositoryException {
        try {
            return new InputStreamReader(getNode(path.toString()).getInputStream(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RepositoryException(e.getMessage(), e);
        }
    }

    public String[] getRevisions(Path path) throws RepositoryException {
        Node node = getNode(path.toString());
        Revision[] revisions = node.getRevisions();
        String[] revisionNames = new String[revisions.length];
        for (int i=0; i<revisions.length; i++) {
            revisionNames[i] = revisions[i].getName();
        }
        return revisionNames;
    }

    public long getSize(Path path) throws RepositoryException {
        return getNode(path.toString()).getSize();
    }

    public UID getUID(Path path) throws RepositoryException {
        log.warn("Not implemented.");
        return null;
    }

    public void getURI(Path path) throws RepositoryException {
        log.warn("Not implemented.");
    }

    public void getValidity(Path path) throws RepositoryException {
        log.warn("Not implemented.");
    }

    public Writer getWriter(Path path) throws RepositoryException {
        try {
            return new OutputStreamWriter(getNode(path.toString()).getOutputStream(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RepositoryException(e.getMessage(), e);
        }
    }

    public boolean isCollection(Path path) throws RepositoryException {
        return getNode(path.toString()).isCollection();
    }

    public boolean isResource(Path path) throws RepositoryException {
        return getNode(path.toString()).isResource();
    }

    ///////////////////////////////////////////////////////////////////////////
    // New methods for node based repository
    ///////////////////////////////////////////////////////////////////////////
    
    protected File contentDir;
    protected File metaDir;
    
    /**
     * @see org.wyona.yarep.core.Repository#copy(java.lang.String, java.lang.String)
     */
    public void copy(String srcPath, String destPath) throws RepositoryException {
        // TODO: not implemented yet
        log.warn("Not implemented yet.");
    }

    /**
     * @see org.wyona.yarep.core.Repository#existsNode(java.lang.String)
     */
    public boolean existsNode(String path) throws RepositoryException {
        // strip trailing slash:
        if (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        if (map.exists(new Path(path))) {
            return true;
        } else if (fallback) {
            log.info("No UID! Fallback to : " + path);
            File file = new File(contentDir + path);
            return file.exists();
        } else {
            return false;
        }
        //return map.exists(new Path(path));
    }

    /**
     * @see org.wyona.yarep.core.Repository#getNode(java.lang.String)
     */
    public Node getNode(String path) throws NoSuchNodeException, RepositoryException {
        // strip trailing slash:
        if (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        String uuid;
        if (!map.exists(new Path(path))) {
            if (fallback) {
                log.info("No UID! Fallback to : " + path);
                if (!(new File(contentDir + path)).exists()) {
                    throw new NoSuchNodeException(path, this);
                }
                uuid = new UID(path).toString();
            } else {
                throw new NoSuchNodeException(path, this);
            }
        } else {
            UID uid = map.getUID(new Path(path));
            uuid = (uid == null) ? path : uid.toString();
            
        }
        
        //String uuid = map.getUID(new Path(path)).toString();
        return new FileSystemNode(this, path, uuid);
    }

    /**
     * @see org.wyona.yarep.core.Repository#getNodeByUUID(java.lang.String)
     */
    public Node getNodeByUUID(String uuid) throws NoSuchNodeException, RepositoryException {
        //String path = map.getPath(uuid);
        //return new FileSystemNode(this, path, uuid);
        // TODO: not implemented yet
        log.warn("Not implemented yet.");
        return null;
    }

    /**
     * @see org.wyona.yarep.core.Repository#getRootNode()
     */
    public Node getRootNode() throws RepositoryException {
        return getNode("/");
    }

    /**
     * @see org.wyona.yarep.core.Repository#move(java.lang.String, java.lang.String)
     */
    public void move(String srcPath, String destPath) throws RepositoryException {
        //map.move(srcPath, destPath);
        // TODO: not implemented yet
        log.warn("Not implemented yet.");
    }
    
    // implementation specific methods:
    
    public File getContentDir() {
        return this.contentDir;
    }
    
    public Map getMap() {
        return this.map;
    }

    /**
     *
     */
    public void close() throws RepositoryException {
        log.warn("Not implemented!");
    }

    /**
     * Search content
     */
    public Node[] search(String query) throws RepositoryException {
        try {
            Searcher searcher = new IndexSearcher(getSearchIndexFile().getAbsolutePath());
            if (searcher != null) {
                try {
                    org.apache.lucene.search.Query luceneQuery = new org.apache.lucene.queryParser.QueryParser("_FULLTEXT", analyzer).parse(query);
                    org.apache.lucene.search.Hits hits = searcher.search(luceneQuery);
                    log.info("Number of matching documents: " + hits.length());
                    java.util.Vector results = new java.util.Vector();
                    for (int i = 0; i < hits.length(); i++) {
                        try {
                            results.addElement(getNode(hits.doc(i).getField("_PATH").stringValue()));
                        } catch (NoSuchNodeException nsne) {
                            log.warn("Found within search index, but no such node within repository: " + hits.doc(i).getField("_PATH").stringValue());
                        }
                    }
                    Node[] res = new Node[results.size()];
                    for (int i = 0; i < res.length; i++) {
                        res[i] = (Node) results.elementAt(i);
                    }
                    return res;
                } catch (Exception e) {
                    log.error(e, e);
                    throw new RepositoryException(e.getMessage());
                }
            } else {
                log.warn("No search index seems to be configured!");
            }
        } catch (Exception e) {
            log.error(e, e);
            throw new RepositoryException(e.getMessage());
        }
        return null;
    }

    /**
     * Get yarep meta directory
     */
    public File getYarepMetaDir() {
        return this.metaDir;
    }

    /**
     *
     */
    public File getSearchIndexFile() {
        return searchIndexFile;
    }

    /**
     *
     */
    public Analyzer getAnalyzer() {
        return analyzer;
    }

    /**
     *
     */
    public boolean isFallbackEnabled() {
        return fallback;
    }
}

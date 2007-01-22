package org.wyona.yarep.impl.repo.fs;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.log4j.Category;
import org.wyona.commons.io.FileUtil;
import org.wyona.yarep.core.Map;
import org.wyona.yarep.core.NoSuchNodeException;
import org.wyona.yarep.core.Node;
import org.wyona.yarep.core.Path;
import org.wyona.yarep.core.Repository;
import org.wyona.yarep.core.RepositoryException;
import org.wyona.yarep.core.Storage;
import org.wyona.yarep.core.UID;

/**
 * Node based file system repository.
 */
public class FileSystemRepository implements Repository {

    private static Category log = Category.getInstance(FileSystemRepository.class);

    protected String id;
    protected File configFile;

    protected String name;

    protected Map map;
    protected Storage storage;

    private boolean fallback = false;

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

            log.debug("content dir: " + this.contentDir);
        } catch (Exception e) {
            log.error(e.toString());
            throw new RepositoryException("Could not read repository configuration: " 
                    + e.getMessage(), e);
        }
    }

    public void addSymbolicLink(Path target, Path link) throws RepositoryException {
        log.warn("Not implemented.");
    }

    public boolean delete(Path path) throws RepositoryException {
        log.warn("Not implemented.");
        return false;
    }

    public boolean exists(Path path) throws RepositoryException {
        log.warn("Not implemented.");
        return false;
    }

    public Path[] getChildren(Path path) throws RepositoryException {
        log.warn("Not implemented.");
        return null;
    }

    public File getConfigFile() {
        log.warn("Not implemented.");
        return null;
    }

    public void getContentLength(Path path) throws RepositoryException {
        log.warn("Not implemented.");
    }

    public String getID() {
        log.warn("Not implemented.");
        return null;
    }

    public InputStream getInputStream(Path path) throws RepositoryException {
        log.warn("Not implemented.");
        return null;
    }

    public long getLastModified(Path path) throws RepositoryException {
        log.warn("Not implemented.");
        return 0;
    }

    public String getName() {
        log.warn("Not implemented.");
        return null;
    }

    public OutputStream getOutputStream(Path path) throws RepositoryException {
        log.warn("Not implemented.");
        return null;
    }

    public Reader getReader(Path path) throws RepositoryException {
        log.warn("Not implemented.");
        return null;
    }

    public String[] getRevisions(Path path) throws RepositoryException {
        log.warn("Not implemented.");
        return null;
    }

    public long getSize(Path path) throws RepositoryException {
        log.warn("Not implemented.");
        return 0;
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
        log.warn("Not implemented.");
        return null;
    }

    public boolean isCollection(Path path) throws RepositoryException {
        log.warn("Not implemented.");
        return false;
    }

    public boolean isResource(Path path) throws RepositoryException {
        log.warn("Not implemented.");
        return false;
    }

    public void setID(String id) {
        log.warn("Not implemented.");
    }

    ///////////////////////////////////////////////////////////////////////////
    // New methods for node based repository
    ///////////////////////////////////////////////////////////////////////////
    
    protected File contentDir;
    
    public void copy(String srcPath, String destPath) throws RepositoryException {
        // TODO: not implemented yet
        log.warn("Not implemented yet.");
    }

    public boolean existsNode(String path) throws RepositoryException {
        return map.exists(new Path(path));
    }

    public Node getNode(String path) throws NoSuchNodeException, RepositoryException {
        String uuid = map.getUID(new Path(path)).toString();
        return new FileSystemNode(this, path, uuid);
    }

    public Node getNodeByUUID(String uuid) throws NoSuchNodeException, RepositoryException {
        //String path = map.getPath(uuid);
        //return new FileSystemNode(this, path, uuid);
        // TODO: not implemented yet
        log.warn("Not implemented yet.");
        return null;
    }

    public Node getRootNode() throws RepositoryException {
        return getNode("/");
    }

    public void move(String srcPath, String destPath) throws RepositoryException {
        //map.move(srcPath, destPath);
        // TODO: not implemented yet
        log.warn("Not implemented yet.");
    }
    
    // implemenation specific methods:
    
    public File getContentDir() {
        return this.contentDir;
    }
    
    public Map getMap() {
        return this.map;
    }


}

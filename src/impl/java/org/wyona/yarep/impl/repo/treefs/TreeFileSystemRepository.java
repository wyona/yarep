package org.wyona.yarep.impl.repo.treefs;

import java.io.File;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.log4j.Category;
import org.wyona.commons.io.FileUtil;
import org.wyona.yarep.core.Map;
import org.wyona.yarep.core.NoSuchNodeException;
import org.wyona.yarep.core.Node;
import org.wyona.yarep.core.Path;
import org.wyona.yarep.core.RepositoryException;
import org.wyona.yarep.core.UID;
import org.wyona.yarep.impl.repo.fs.FileSystemRepository;
import org.wyona.yarep.impl.repo.treefs.map.TreeFileSystemMap;


/**
 * 
 * This implementation extends the FileSystemRepository and should provide better scaling
 * with a large number of documents.
 * 
 * To avoid a large number of directories on the same level, this implementation splits 
 * the uuid and creates a tree as follows:
 * 
 * Example:
 *   uuids: 123456, 123457, 456789
 *   directories:
 *   /content-root/12/34/56
 *   /content-root/12/34/57
 *   /content-root/45/67/89
 * 
 * The splitting can be configured by two parameters:
 *   split-interval: number of characters on each level (two in the above example)
 *   max-splits: maximum number of splittings 
 * 
 */
public class TreeFileSystemRepository extends FileSystemRepository {

    private static Category log = Category.getInstance(TreeFileSystemRepository.class);

    protected int splitInterval;
    protected int maxSplits;

    /**
     *
     */
    public TreeFileSystemRepository() {
    }
    
    /**
     *
     */
    public TreeFileSystemRepository(String id, File configFile) throws RepositoryException {
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

            splitInterval = config.getChild("split-interval", false).getValueAsInteger();
            maxSplits = config.getChild("max-splits", false).getValueAsInteger();
            
            if (map instanceof TreeFileSystemMap) {
                ((TreeFileSystemMap)map).setSplitInterval(splitInterval);
                ((TreeFileSystemMap)map).setMaxSplits(maxSplits);
            }

            log.debug("content dir: " + this.contentDir);
        } catch (Exception e) {
            log.error(e.toString());
            throw new RepositoryException("Could not read repository configuration: " 
                    + e.getMessage(), e);
        }
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
        } else {
            return false;
        }
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
            throw new NoSuchNodeException(path, this);
        } else {
            UID uid = map.getUID(new Path(path));
            uuid = (uid == null) ? path : uid.toString();
        }
        
        return new TreeFileSystemNode(this, path, uuid);
    }

    public int getSplitInterval() {
        return this.splitInterval;
    }

    public int getMaxSplits() {
        return this.maxSplits;
    }

}

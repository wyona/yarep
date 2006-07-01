package org.wyona.yarep.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;

import org.apache.log4j.Category;

import org.wyona.commons.io.FileUtil;

/**
 *
 */
public class Repository {

    private static Category log = Category.getInstance(Repository.class);

    protected String id;
    protected File configFile;

    protected String name;

    protected File pathsDir;

    protected Map map;
    protected Storage storage;

    /**
     *
     */
    public Repository(String id, File configFile) {
        this.id = id;
        this.configFile = configFile;

        readConfiguration();
    }

    /**
     * Read respectively load repository configuration
     */
    private void readConfiguration() {
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
                map.readConfig(pathConfig, configFile);
                log.debug(map.getClass().getName());
            } else {
                log.error("DEBUG: No class attribute ...");
            }

            pathsDir = new File(pathConfig.getAttribute("src"));
            if (!pathsDir.isAbsolute()) {
                pathsDir = FileUtil.file(configFile.getParent(), pathsDir.toString());
            }
            log.debug(pathsDir.toString());
            // TODO: Throw Exception
            if (!pathsDir.exists()) log.error("No such file or directory: " + pathsDir);

            Configuration storageConfig = config.getChild("storage", false);

            String storageClassname = storageConfig.getAttribute("class", null);
            log.debug(storageClassname);
            Class storageClass = Class.forName(storageClassname);
            storage = (Storage) storageClass.newInstance();
            storage.readConfig(storageConfig, configFile);
            log.debug(storage.getClass().getName());
        } catch (Exception e) {
            log.error(e.toString());
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

    /**
     *
     */
    public Writer getWriter(Path path) {
        UID uid = getUID(path);
        log.debug(uid.toString());
        return storage.getWriter(uid, path);
    }

    /**
     *
     */
    public OutputStream getOutputStream(Path path) {
        UID uid = getUID(path);
        log.debug(uid.toString());
        return storage.getOutputStream(uid, path);
    }

    /**
     *
     */
    public Reader getReader(Path path) throws NoSuchNodeException {
        if (!exists(path)) throw new NoSuchNodeException(path);
        UID uid = getUID(path);
        log.debug(uid.toString());
        return storage.getReader(uid, path);
    }

    /**
     *
     */
    public InputStream getInputStream(Path path) throws NoSuchNodeException {
        if (!exists(path)) throw new NoSuchNodeException(path);
        UID uid = getUID(path);
        log.debug(uid.toString());
        return storage.getInputStream(uid, path);
    }

    /**
     *
     */
    public long getLastModified(Path path) {
        UID uid = getUID(path);
        return storage.getLastModified(uid, path);
    }

    /**
     * Not implemented yet
     * http://excalibur.apache.org/apidocs/org/apache/excalibur/source/impl/FileSource.html#getValidity()
     * http://excalibur.apache.org/apidocs/org/apache/excalibur/source/SourceValidity.html
     */
    public void getValidity(Path path) {
    }

    /**
     * Not implemented yet
     * http://excalibur.apache.org/apidocs/org/apache/excalibur/source/impl/FileSource.html#getContentLength()
     */
    public void getContentLength(Path path) {
    }

    /**
     * Not implemented yet
     * http://excalibur.apache.org/apidocs/org/apache/excalibur/source/impl/FileSource.html#getURI()
     */
    public void getURI(Path path) {
    }

    /**
     * NOTE: Depends in the future on specific map implementation
     */
    public boolean isResource(Path path) {
        File file = new File(pathsDir + path.toString());
        File uidFile = new File(pathsDir + path.toString() + File.separator + ".yarep-uid");
        return uidFile.exists() || file.isFile();
    }

    /**
     * One might want to discuss what is a collection. A resource for instance could
     * also be a collection, but a collection with some default content.
     * In the case of JCR there are only nodes and properties!
     */
    public boolean isCollection(Path path) {
        File file = new File(pathsDir + path.toString());
        return (file.exists() && !isResource(path));
        //return true;
    }

    /**
     *
     */
    public boolean exists(Path path) {
        File file = new File(pathsDir + path.toString());
        log.debug("Path (" + getName() + "): " + file);
        return file.exists();
    }

    /**
     *
     */
    public Path[] getChildren(Path path) {
        File file = new File(pathsDir + path.toString());
        String[] filenames = file.list();

	// NOTE: This situation should only occur if isResource(Path) didn't work properly!
        if (filenames == null) {
            log.warn("No children: " + path + " (" + file + ")");
            return new Path[0];
        }

        log.debug("Number of children: " + filenames.length + " (" + file + ")");
        Path[] children = new Path[filenames.length];
        for (int i = 0;i < children.length; i++) {
            if (path.toString().endsWith(File.separator)) {
                children[i] = new Path(path + filenames[i]);
            } else {
                // NOTE: Do not use File.separator here, because it's the repository path and not the Operating System File System path
                children[i] = new Path(path + "/" + filenames[i]);
            }
            log.debug("Child: " + children[i]);
        }
        return children;
    }

    /**
     * Get UID
     *
     * http://www.ietf.org/rfc/rfc4122.txt
     * http://incubator.apache.org/jackrabbit/apidocs/org/apache/jackrabbit/uuid/UUID.html
     * http://www.webdav.org/specs/draft-leach-uuids-guids-01.txt
     */
    public synchronized UID getUID(Path path) {
        log.debug(pathsDir.toString());
        File uidFile = new File(pathsDir + path.toString() + File.separator + ".yarep-uid");
        log.debug(uidFile.toString());
        if (uidFile.exists()) {
            try {
                FileReader fr = new FileReader(uidFile);
                BufferedReader br = new BufferedReader(fr);
                String existingUID = br.readLine();
                br.close();
                fr.close();
                return new UID(existingUID);
            } catch (Exception e) {
                log.error(e);
                throw new RuntimeException(e);
            }
        }

        // TODO: Shouldn't the uid be written only if the writer is being closed successfully!
	//String uid = "" + System.currentTimeMillis();
	String uuid = org.apache.commons.id.uuid.UUID.randomUUID().toString();
	//String uid = java.util.UUID.randomUUID().toString(); // Java 1.5.x
        try {
            File parent = new File(uidFile.getParent());
            if (!parent.exists()) {
                log.warn("Directory will be created: " + parent);
                parent.mkdirs();
            }
            // TODO: ...
            if (parent.isFile()) {
                log.warn("Parent is a file and not a directory: " + parent);
            }
            FileWriter fw = new FileWriter(uidFile);
            fw.write(uuid);
            fw.close();
        } catch (Exception e) {
            log.error(e);
        }
        return new UID(uuid);
    }
}

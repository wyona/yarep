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
            } else {
                map = new org.wyona.yarep.impl.DefaultMapImpl();
            }
            map.readConfig(pathConfig, configFile);

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
     *
     */
    public boolean isResource(Path path) {
        return map.isResource(path);
    }

    /**
     * One might want to discuss what is a collection. A resource for instance could
     * also be a collection, but a collection with some default content.
     * In the case of JCR there are only nodes and properties!
     */
    public boolean isCollection(Path path) {
        return map.isCollection(path);
    }

    /**
     *
     */
    public boolean exists(Path path) {
       return map.exists(path);
    }

    /**
     *
     */
    public Path[] getChildren(Path path) {
        return map.getChildren(path);
    }

    /**
     * Get UID
     *
     * http://www.ietf.org/rfc/rfc4122.txt
     * http://incubator.apache.org/jackrabbit/apidocs/org/apache/jackrabbit/uuid/UUID.html
     * http://www.webdav.org/specs/draft-leach-uuids-guids-01.txt
     */
    public synchronized UID getUID(Path path) {
        return map.getUID(path);
    }
}

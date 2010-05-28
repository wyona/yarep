/**
 * Copyright 2009 Wyona
 */
package org.wyona.yarep.impl.repo.orm;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import org.wyona.yarep.core.Node;
import org.wyona.yarep.core.NoSuchNodeException;
import org.wyona.yarep.core.Path;
import org.wyona.yarep.core.Repository;
import org.wyona.yarep.core.RepositoryException;
import org.wyona.yarep.core.UID;
import org.wyona.yarep.core.search.Indexer;
import org.wyona.yarep.core.search.Searcher;

import org.apache.log4j.Logger;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;

import org.apache.commons.dbcp.BasicDataSource;

/**
 * Object Relational Mapping repository implementation
 * @see org.wyona.yarep.core.Repository
 */
public class ORMRepositoryImpl implements Repository {

    private static final Logger log = Logger.getLogger(ORMRepositoryImpl.class);

    protected String repoId;
    protected String repoName;
    protected File configFile;

    private String driverClassName;
    private String url;
    private String username;
    private String password;

    /**
     *
     */
    public BasicDataSource getBasicDataSource() {
        BasicDataSource bds = new BasicDataSource();
        bds.setDriverClassName(driverClassName);
        bds.setUrl(url);
        bds.setUsername(username);
        bds.setPassword(password);
        return bds;
    }

    /**
     * @see org.wyona.yarep.core.Repository#getID()
     */
    public String getID() {
        return repoId;
    }

    /**
     * @see org.wyona.yarep.core.Repository#setID(String)
     */
    public void setID(String id) {
        this.repoId = id;
    }

    /**
     * @see org.wyona.yarep.core.Repository#readConfiguration(File)
     */
    public void readConfiguration(File configFile) throws RepositoryException {
        this.configFile = configFile;
        DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder(true);
        Configuration config;
        try {
            config = builder.buildFromFile(configFile);
            repoName = config.getChild("name", false).getValue();

            Configuration dataSourceConfig = config.getChild("data-source", false);
            driverClassName = dataSourceConfig.getChild("driverClassName").getValue();
            url = dataSourceConfig.getChild("url").getValue();
            username = dataSourceConfig.getChild("username").getValue();
            password = dataSourceConfig.getChild("password").getValue("");
            if (password == null || password.length() <= 0) {
                log.warn("No password set: " + configFile);
            }
        } catch(Exception e) {
            log.error(e, e);
            throw new RepositoryException("Could not read repository configuration: " + e.getMessage(), e);
        }
    }

    /**
     * @see org.wyona.yarep.core.Repository#getName()
     */
    public String getName() {
        return repoName;
    }

    /**
     * @see org.wyona.yarep.core.Repository#getConfigFile()
     */
    public File getConfigFile() {
        log.warn("TODO: Not implemented yet!");
        return null;
    }

    /**
     * @see org.wyona.yarep.core.Repository#getWriter(Path)
     */
    public Writer getWriter(Path path) throws RepositoryException {
        log.warn("TODO: Not implemented yet!");
        return null;
    }

    /**
     * @see org.wyona.yarep.core.Repository#getOutputStream(Path)
     */
    public OutputStream getOutputStream(Path path) throws RepositoryException {
        log.warn("TODO: Not implemented yet!");
        return null;
    }

    /**
     * @see org.wyona.yarep.core.Repository#getReader(Path)
     */
    public Reader getReader(Path path) throws RepositoryException {
        log.warn("TODO: Not implemented yet!");
        return null;
    }

    /**
     * @see org.wyona.yarep.core.Repository#getInputStream(Path)
     */
    public InputStream getInputStream(Path path) throws RepositoryException {
        log.warn("TODO: Not implemented yet!");
        return null;
    }

    /**
     * @see org.wyona.yarep.core.Repository#getLastModified(Path)
     */
    public long getLastModified(Path path) throws RepositoryException {
        log.warn("TODO: Not implemented yet!");
        return -1;
    }
    
    /**
     * @see org.wyona.yarep.core.Repository#getSize(Path)
     */
    public long getSize(Path path) throws RepositoryException {
        log.warn("TODO: Not implemented yet!");
        return -1;
    }
    
    /**
     * @see org.wyona.yarep.core.Repository#delete(Path)
     */
    public boolean delete(Path path) throws RepositoryException {
        log.warn("TODO: Not implemented yet!");
        return false;
    }
    
    /**
     * @see org.wyona.yarep.core.Repository#delete(Path, boolean)
     */
    public boolean delete(Path path, boolean recursive) throws RepositoryException {
        log.warn("TODO: Not implemented yet!");
        return false;
    }

    /**
     * @see org.wyona.yarep.core.Repository#getValidity(Path)
     */
    public void getValidity(Path path) throws RepositoryException {
        log.warn("TODO: Not implemented yet!");
    }

    /**
     * @see org.wyona.yarep.core.Repository#getContentLength(Path)
     */
    public void getContentLength(Path path) throws RepositoryException {
        log.warn("TODO: Not implemented yet!");
    }

    /**
     * @see org.wyona.yarep.core.Repository#getURI(Path)
     */
    public void getURI(Path path) throws RepositoryException {
        log.warn("TODO: Not implemented yet!");
    }

    /**
     * @see org.wyona.yarep.core.Repository#isResource(Path)
     */
    public boolean isResource(Path path) throws RepositoryException {
        log.warn("TODO: Not implemented yet!");
        return false;
    }

    /**
     * @see org.wyona.yarep.core.Repository#isCollection(Path)
     */
    public boolean isCollection(Path path) throws RepositoryException {
        log.warn("TODO: Not implemented yet!");
        return false;
    }

    /**
     * @see org.wyona.yarep.core.Repository#getChildren(Path)
     */
    public Path[] getChildren(Path path) throws RepositoryException {
        log.warn("TODO: Not implemented yet!");
        return null;
    }

    /**
     * @see org.wyona.yarep.core.Repository#getUID(Path)
     */
    public UID getUID(Path path) throws RepositoryException {
        log.warn("TODO: Not implemented yet!");
        return null;
    }
    
    /**
     * @see org.wyona.yarep.core.Repository#getRevisions(Path)
     */
    public String[] getRevisions(Path path) throws RepositoryException {
        log.warn("TODO: Not implemented yet!");
        return null;
    }

    /**
     * @see org.wyona.yarep.core.Repository#addSymbolicLink(Path, Path)
     */
    public void addSymbolicLink(Path target, Path link) throws RepositoryException {
        log.warn("TODO: Not implemented yet!");
    }
    
    /**
     * @see org.wyona.yarep.core.Repository#getNode(String)
     */
    public Node getNode(String path) throws NoSuchNodeException, RepositoryException {
        log.warn("TODO: Not implemented yet!");
        return null;
    }
    
    /**
     * @see org.wyona.yarep.core.Repository#getNodeByUUID(String)
     */
    public Node getNodeByUUID(String uuid) throws NoSuchNodeException, RepositoryException {
        log.warn("TODO: Not implemented yet!");
        return null;
    }
    
    /**
     * @see org.wyona.yarep.core.Repository#exists(String)
     */
    public boolean existsNode(String path) throws RepositoryException {
        log.warn("TODO: Not implemented yet!");
        return false;
    }

    /**
     * @see org.wyona.yarep.core.Repository#exists(Path)
     */
    public boolean exists(Path path) throws RepositoryException {
        log.warn("TODO: Not implemented yet!");
        return false;
    }
    
    /**
     * @see org.wyona.yarep.core.Repository#getRootNode()
     */
    public Node getRootNode() throws RepositoryException {
        log.warn("TODO: Not implemented yet!");
        return null;
    }
    
    /**
     * @see org.wyona.yarep.core.Repository#copy(String, String)
     */
    public void copy(String srcPath, String destPath) throws RepositoryException {
        log.warn("TODO: Not implemented yet!");
    }
    
    /**
     * @see org.wyona.yarep.core.Repository#move(String, String)
     */
    public void move(String srcPath, String destPath) throws RepositoryException {
        log.warn("TODO: Not implemented yet!");
    }

    /**
     * @see org.wyona.yarep.core.Repository#search(String)
     */
    public Node[] search(String query) throws RepositoryException {
        log.warn("TODO: Not implemented yet!");
        return null;
    }
    
    /**
     * @see org.wyona.yarep.core.Repository#searchProperty(String, String, String)
     */
    public Node[] searchProperty(String pName, String pValue, String path) throws RepositoryException {
        log.warn("TODO: Not implemented yet!");
        return null;
    }
    
    /**
     * @see org.wyona.yarep.core.Repository#close()
     */
    public void close() throws RepositoryException {
        log.warn("TODO: Not implemented yet!");
    }
    
    /**
     * @see org.wyona.yarep.core.Repository#getIndexer()
     */
    public Indexer getIndexer() throws RepositoryException {
        log.warn("TODO: Not implemented yet!");
        return null;
    }

    /**
     * @see org.wyona.yarep.core.Repository#getSearcher()
     */
    public Searcher getSearcher() throws RepositoryException {
        log.warn("TODO: Not implemented yet!");
        return null;
    }

    /**
     * @see org.wyona.yarep.core.Repository#importNode(String, String Repository)
     */
    public boolean importNode(String destPath, String srcPath, Repository srcRepository) throws RepositoryException {
        log.warn("TODO: Not implemented yet!");
        return false;
    }
}

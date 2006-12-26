package org.wyona.yarep.impl.repo.orm;

import org.wyona.yarep.core.Path;
import org.wyona.yarep.core.RepositoryException;
import org.wyona.yarep.core.Storage;
import org.wyona.yarep.core.UID;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.log4j.Category;

/**
 *
 */
public class ORMStorage implements Storage {

    private static Category log = Category.getInstance(ORMStorage.class);

    /**
     *
     */
    public void readConfig(Configuration storageConfig, File repoConfigFile) throws RepositoryException {
        try {
/*
            Class.forName("org.postgresql.Driver");
            String username = "univers";
            String password = "";
            java.sql.Connection con = java.sql.DriverManager.getConnection("jdbc:postgresql:import_dev://127.0.0.1:5432", username, password);
*/
            Class.forName("org.hsqldb.jdbcDriver");
            String username = "sa";
            String password = "";
            java.sql.Connection con = java.sql.DriverManager.getConnection("jdbc:hsqldb:hsql://127.0.0.1:9001", username, password);
        } catch(Exception e) {
            log.error(e.getMessage());
            //throw new RepositoryException(e);
        }
    }

    /**
     *@deprecated
     */
    public Writer getWriter(UID uid, Path path) {
        log.warn("Not implemented yet!");
        return null;
    }

    /**
     *
     */
    public OutputStream getOutputStream(UID uid, Path path) throws RepositoryException {
        log.warn("Not implemented yet!");
        return null;
    }

    /**
     *@deprecated
     */
    public Reader getReader(UID uid, Path path) {
        log.warn("Not implemented yet!");
        return null;
    }

    /**
     *
     */
    public InputStream getInputStream(UID uid, Path path) throws RepositoryException {
        log.warn("Not implemented yet!");
        return null;
    }

    /**
     *
     */
    public long getLastModified(UID uid, Path path) throws RepositoryException {
        log.warn("Not implemented yet!");
        return 0;
    }
    
    /**
     *
     */
    public long getSize(UID uid, Path path) throws RepositoryException {
    	log.warn("Not implemented yet!");
    	return 0;
    }

    /**
     *
     */
    public boolean delete(UID uid, Path path) throws RepositoryException {
        log.error("TODO: Not implemented yet!");
        return false;
    }
    
    /**
     * 
     */
    public String[] getRevisions(UID uid, Path path) throws RepositoryException {
        log.warn("Versioning not implemented yet");
        return null;
    }

}

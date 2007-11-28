package org.wyona.yarep.core.impl.fs;

import org.wyona.commons.io.FileUtil;
import org.wyona.yarep.core.NoSuchNodeException;
import org.wyona.yarep.core.Path;
import org.wyona.yarep.core.RepositoryException;
import org.wyona.yarep.core.Storage;
import org.wyona.yarep.core.UID;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Category;

/**
 *
 */
public class FileSystemStorage implements Storage {

    private static Category log = Category.getInstance(FileSystemStorage.class);

    private File contentDir;

    /**
     *
     */
    public void readConfig(Configuration storageConfig, File repoConfigFile) throws RepositoryException {
        Configuration contentConfig = storageConfig.getChild("content", false);
        try {
            contentDir = new File(contentConfig.getAttribute("src"));
            log.debug(contentDir.toString());
            log.debug(repoConfigFile.toString());
            if (!contentDir.isAbsolute()) {
                contentDir = FileUtil.file(repoConfigFile.getParent(), contentDir.toString());
            }
            log.debug(contentDir.toString());
            //TODO: Throw an exception
            if (!contentDir.exists()) log.error("No such file or directory: " + contentDir);
        } catch (Exception e) {
            log.error(e);
            throw new RepositoryException("Could not read repository configuration: " 
                    + repoConfigFile.getAbsolutePath() + " " + e.getMessage(),  e);
        }
    }

    /**
     *@deprecated
     */
    public Writer getWriter(UID uid, Path path) {
        return new FileSystemRepositoryWriter(uid, path, contentDir);
    }

    /**
     *
     */
    public OutputStream getOutputStream(UID uid, Path path) throws RepositoryException {
        return new FileSystemRepositoryOutputStream(uid, path, contentDir);
    }

    /**
     *@deprecated
     */
    public Reader getReader(UID uid, Path path) throws NoSuchNodeException {
        return new FileSystemRepositoryReader(uid, path, contentDir);
    }

    /**
     *
     */
    public InputStream getInputStream(UID uid, Path path) throws RepositoryException {
        return new FileSystemRepositoryInputStream(uid, path, contentDir);
    }

    /**
     *
     */
    public long getLastModified(UID uid, Path path) throws RepositoryException {
        File file = new File(contentDir.getAbsolutePath() + File.separator + uid.toString());
        return file.lastModified(); 
    }
    
    /**
     * @return Size of file in bytes
     */
    public long getSize(UID uid, Path path) throws RepositoryException {
    	File file = new File(contentDir.getAbsolutePath() + File.separator + uid.toString());
    	return file.length(); 
    }
    
    /**
     *
     */
    public boolean delete(UID uid, Path path) throws RepositoryException {
        File file = new File(contentDir.getAbsolutePath() + File.separator + uid.toString());
        log.debug("Try to delete: " + file);
        try {
            if (!file.exists()) {
                log.warn("file " + file + " " + uid + " cannot be deleted because it does not exist.");
                return true;
            }
            if (file.isDirectory()) {
                FileUtils.deleteDirectory(file);
                return true;
            } else {
                return file.delete();
            }
        } catch (IOException e) {
            log.error("could not delete " + uid + " " + path + ": " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 
     */
    public String[] getRevisions(UID uid, Path path) throws RepositoryException {
        log.warn("Versioning not implemented yet");
        return null;
    }

}

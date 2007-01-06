package org.wyona.yarep.core.impl.vfs;

import org.wyona.commons.io.FileUtil;
import org.wyona.yarep.core.NoSuchNodeException;
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
public class VFileSystemStorage implements Storage {

    private static Category log = Category.getInstance(VFileSystemStorage.class);

    private File contentDir;
    private String alternative = null;

    /**
     * Read VFS Storage configuration
     */
    public void readConfig(Configuration storageConfig, File repoConfigFile) throws RepositoryException {
        Configuration contentConfig = storageConfig.getChild("content", false);
        Configuration directoryConfig = storageConfig.getChild("directory", false);
        try {
            contentDir = new File(contentConfig.getAttribute("src"));
            log.debug(contentDir.toString());
            log.debug(repoConfigFile.toString());
            if (!contentDir.isAbsolute()) {
                contentDir = FileUtil.file(repoConfigFile.getParent(), contentDir.toString());
            }
            log.debug(contentDir.toString());
            // TODO: Throw an exception
            if (!contentDir.exists()) log.error("No such file or directory: " + contentDir);

            if (directoryConfig != null) {
                alternative = directoryConfig.getAttribute("alternative", null);
                log.debug("Alternative: " + alternative);
            }
        } catch (Exception e) {
            log.error(e);
            throw new RepositoryException(e.getMessage(), e);
        }
    }

    /**
     *
     */
    public Writer getWriter(UID uid, Path path) {
        return new VFileSystemRepositoryWriter(uid, path, contentDir);
    }

    /**
     *
     */
    public OutputStream getOutputStream(UID uid, Path path) throws RepositoryException {
        return new VFileSystemRepositoryOutputStream(uid, path, contentDir);
    }

    /**
     *
     */
    public Reader getReader(UID uid, Path path) throws NoSuchNodeException {
        return new VFileSystemRepositoryReader(uid, path, contentDir);
    }

    /**
     *
     */
    public InputStream getInputStream(UID uid, Path path) throws RepositoryException {
        return new VFileSystemRepositoryInputStream(uid, path, contentDir, alternative);
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
        return file.delete();
    }
    
    /**
     * 
     */
    public String[] getRevisions(UID uid, Path path) throws RepositoryException {
        log.warn("Versioning not implemented yet");
        return null;
    }

}

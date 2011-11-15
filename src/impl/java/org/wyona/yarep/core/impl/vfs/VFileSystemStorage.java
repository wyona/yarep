package org.wyona.yarep.core.impl.vfs;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.log4j.Category;
import org.wyona.commons.io.FileUtil;
import org.wyona.yarep.core.NoSuchNodeException;
import org.wyona.yarep.core.Path;
import org.wyona.yarep.core.RepositoryException;
import org.wyona.yarep.core.Storage;
import org.wyona.yarep.core.UID;

/**
 *
 */
public class VFileSystemStorage implements Storage {

    private static Category log = Category.getInstance(VFileSystemStorage.class);

    private File contentDir;
    private String alternative = null;
    private String dirListingMimeType = "application/xml";

    // Configuration parameters of the <splitpath ...> element
    private SplitPathConfig splitPathConfig = new SplitPathConfig();
    private boolean splitPathEnabled = false;
    
    

    /**
     * Read VFS Storage configuration
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
            // TODO: Throw an exception
            if (!contentDir.exists()) log.error("No such file or directory: " + contentDir);

            Configuration directoryConfig = storageConfig.getChild("directory", false);
            if (directoryConfig != null) {
                alternative = directoryConfig.getAttribute("alternative", null);
                log.debug("Alternative: " + alternative);
                dirListingMimeType = directoryConfig.getAttribute("mime-type", "application/xml");
                log.debug("Mime type of directory listing: " + dirListingMimeType);
            }

            // Read the <splitpath> configuration
            log.debug("Reading Split Path Configuation");
            Configuration splitConfig = storageConfig.getChild("splitpath", false);
            if (splitConfig != null) {
                splitPathEnabled = true;
                splitPathConfig.setSplitparts(Integer.parseInt(splitConfig.getAttribute("depth", "0")));
                splitPathConfig.setSplitlength(Integer.parseInt(splitConfig.getAttribute("length", "0")));
                splitPathConfig.setEscapeChar(splitConfig.getAttribute("escape", "-"));

                int numberOfIncludePaths = splitConfig.getChildren("include").length;
                int i = 0;
                if (numberOfIncludePaths > 0) {
                    String[] includepaths = new String[numberOfIncludePaths];
                    for (Configuration include : splitConfig.getChildren("include")) {
                        includepaths[i++] = include.getAttribute("path");
                    }
                    splitPathConfig.setIncludepaths(includepaths);
                }
                log.debug("Split Path Configuration DONE");
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
        // TODO We pass null as uid argument because the class anyway does not need it at this moment. 
        // If in future this class is going to process the uid argument too, the splitpathConfig object can be passed to it
        String maybeSplitedPath = SplitPathConfig.splitPathIfRequired(path.toString(), this.splitPathConfig);
        Path newPath = new Path(maybeSplitedPath);
        Writer writer = null;
        try {
            writer = new VFileSystemRepositoryWriter(null, newPath, contentDir);
        } catch (Exception e) {
        }
        if (writer == null) {
            writer = new VFileSystemRepositoryWriter(null, path, contentDir);
        }
        return writer;
    }

    /**
     * @param uid is ignored in this implementation!
     */
    public OutputStream getOutputStream(UID uid, Path path) throws RepositoryException {
        // TODO We pass null as uid argument because the class anyway does not need it at this moment. 
        // If in future this class is going to process the uid argument too, the splitpathConfig object can be passed to it
        String maybeSplitedPath = SplitPathConfig.splitPathIfRequired(path.toString(), this.splitPathConfig);
        Path newPath = new Path(maybeSplitedPath);
        OutputStream out = null;
        try {
            out = new VFileSystemRepositoryOutputStream(null, newPath, contentDir);
        } catch (Exception e) {
        }
        if (out == null) {
            out = new VFileSystemRepositoryOutputStream(null, path, contentDir);
        }
        return out;
    }

    /**
     *
     */
    public Reader getReader(UID uid, Path path) throws NoSuchNodeException {
        // TODO We pass null as uid argument because the class anyway does not need it at this moment. 
        // If in future this class is going to process the uid argument too, the splitpathConfig object can be passed to it
        String maybeSplitedPath = SplitPathConfig.splitPathIfRequired(path.toString(), this.splitPathConfig);
        Path newPath = new Path(maybeSplitedPath);
        Reader reader = null;
        try {
            reader = new VFileSystemRepositoryReader(null, newPath, contentDir);
        } catch (Exception e) {
        }
        if (reader == null) {
            reader = new VFileSystemRepositoryReader(null, path, contentDir);
        }
        return reader;
    }

    /**
     * @param uid is ignored in this implementation!
     */
    public InputStream getInputStream(UID uid, Path path) throws RepositoryException {
        // TODO: if uid is processed by VFileSystemRepositoryInputStream, the splitPathConfig must be passed to it too. 
        String maybeSplitedPath = SplitPathConfig.splitPathIfRequired(path.toString(), this.splitPathConfig);
        Path newPath = new Path(maybeSplitedPath);
        InputStream in = null;
        try {
            in = new VFileSystemRepositoryInputStream(null, newPath, contentDir, alternative, dirListingMimeType);
        } catch (Exception e) {
        }
        if (in == null) {
            in = new VFileSystemRepositoryInputStream(null, path, contentDir, alternative, dirListingMimeType);
        }
        return in;
    }

    /**
     * @param path is currently ignored!!
     */
    public long getLastModified(UID uid, Path path) throws RepositoryException {
        String maybeSplitedPath = SplitPathConfig.splitPathIfRequired(uid.toString(), this.splitPathConfig);
        File file = new File(contentDir.getAbsolutePath() + File.separator + maybeSplitedPath);
        if (!file.exists()) {
            file = new File(contentDir.getAbsolutePath() + File.separator + uid.toString());
        }
        return file.lastModified(); 
    }
    
    /**
     * @param path is currently not used!
     * @return Size of file in bytes
     */
    public long getSize(UID uid, Path path) throws RepositoryException {
        String maybeSplitedPath = SplitPathConfig.splitPathIfRequired(uid.toString(), this.splitPathConfig);
        File file = new File(contentDir.getAbsolutePath() + File.separator + maybeSplitedPath);
        if (!file.exists()) {
            file = new File(contentDir.getAbsolutePath() + File.separator + uid.toString());
        }
        return file.length(); 
    }

    /**
     * @param path is currently not used!
     */
    public boolean delete(UID uid, Path path) throws RepositoryException {
        String maybeSplitedPath = SplitPathConfig.splitPathIfRequired(uid.toString(), this.splitPathConfig);
        File file = new File(contentDir.getAbsolutePath() + File.separator + maybeSplitedPath.toString());
        if (!file.exists()) {
            file = new File(contentDir.getAbsolutePath() + File.separator + uid.toString());
        }
        log.debug("Try to delete: " + file);
        return file.delete();
    }
    
    /**
     * Not implemented at this moment
     */
    public String[] getRevisions(UID uid, Path path) throws RepositoryException {
        log.warn("Versioning not implemented yet");
        return null;
    }

    /**
     * Checks the existence via uid first and then via path parameter
     */
    public boolean exists(UID uid, Path path) {
        boolean exists = false;
        if (uid != null) {
            if (splitPathEnabled) {
                File normalFile = new File(contentDir.getAbsolutePath() + File.separator + uid.toString());
                File splitFile = new File(contentDir.getAbsolutePath() + File.separator + SplitPathConfig.splitPathIfRequired(uid.toString(), this.splitPathConfig));
                exists = normalFile.exists() || splitFile.exists();
            } else {
                exists = new File(contentDir.getAbsolutePath() + File.separator + uid.toString()).exists();
            }

        } else if (path != null) {
            log.warn("No UUID specified, hence check path: " + path + " (Content dir: " + contentDir + ")");
            if (splitPathEnabled) {
                File normalFile = new File(contentDir.getAbsolutePath() + File.separator + path.toString());
                File splitFile = new File(contentDir.getAbsolutePath() + File.separator + SplitPathConfig.splitPathIfRequired(path.toString(), this.splitPathConfig));
                exists = normalFile.exists() || splitFile.exists();
            } else {
                exists = new File(contentDir.getAbsolutePath() + File.separator + path.toString()).exists();
            }

        } 
        return exists;
    }
    

}

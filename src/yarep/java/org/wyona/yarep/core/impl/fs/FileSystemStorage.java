package org.wyona.yarep.core.impl.fs;

import org.wyona.util.FileUtil;
import org.wyona.yarep.core.Path;
import org.wyona.yarep.core.Storage;
import org.wyona.yarep.core.UID;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;

import org.apache.avalon.framework.configuration.Configuration;
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
    public void readConfig(Configuration storageConfig, File repoConfigFile) {
        Configuration contentConfig = storageConfig.getChild("content", false);
        try {
            contentDir = new File(contentConfig.getAttribute("src"));
            log.debug(contentDir.toString());
            log.debug(repoConfigFile.toString());
            if (!contentDir.isAbsolute()) {
                contentDir = FileUtil.file(repoConfigFile.getParent(), contentDir.toString());
            }
            log.debug(contentDir.toString());
        } catch (Exception e) {
            log.error(e);
        }
    }

    /**
     *
     */
    public Writer getWriter(UID uid, Path path) {
        return new FileSystemRepositoryWriter(uid, path, contentDir);
    }

    /**
     *
     */
    public Reader getReader(UID uid, Path path) {
        return new FileSystemRepositoryReader(uid, path, contentDir);
    }

    /**
     *
     */
    public InputStream getInputStream(UID uid, Path path) {
        return new FileSystemRepositoryInputStream(uid, path, contentDir);
    }
}

package org.wyona.yarep.core.impl.vfs;

import org.wyona.yarep.core.Path;
import org.wyona.yarep.core.RepositoryException;
import org.wyona.yarep.core.UID;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.log4j.Category;

/**
 *
 */
public class VFileSystemRepositoryOutputStream extends OutputStream {

    private static Category log = Category.getInstance(VFileSystemRepositoryOutputStream.class);

    private FileOutputStream out;

    /**
     *
     */
    public VFileSystemRepositoryOutputStream(UID uid, Path path, File contentDir) throws RepositoryException {
        try {
            File file = new File(contentDir.getAbsolutePath() + path.toString());
            log.debug(file.toString());
            out = new FileOutputStream(file);
        } catch (Exception e) {
            log.error(e);
            throw new RepositoryException(e.getMessage(), e);
        }
    }

    /**
     *
     */
    public void write(int b) throws IOException {
        log.debug("WRITE");
        out.write(b);
    }

    /**
     *
     */
    public void close() throws IOException {
        log.debug("CLOSE");
        out.close();
    }
}

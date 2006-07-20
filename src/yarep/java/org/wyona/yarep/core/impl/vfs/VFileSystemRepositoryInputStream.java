package org.wyona.yarep.core.impl.vfs;

import org.wyona.yarep.core.Path;
import org.wyona.yarep.core.UID;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Category;

/**
 *
 */
public class VFileSystemRepositoryInputStream extends InputStream {

    private static Category log = Category.getInstance(VFileSystemRepositoryInputStream.class);

    private FileInputStream in;

    /**
     *
     */
    public VFileSystemRepositoryInputStream(UID uid, Path path, File contentDir) {
        try {
            File file = new File(contentDir.getAbsolutePath() + path.toString());
            log.debug(file.toString());

            if (file.exists()) {
                if (file.isFile()) {
                    in = new FileInputStream(file);
                } else {
                    log.warn("Is not a file (is probably a directory): " + file);
                    // TODO: Return a list of subdirectories and files
                    in = null;
                }
            } else {
                log.warn("No such file or directory: " + file);
                in = null;
            }
        } catch (Exception e) {
            log.error(e);
            in = null;
        }
    }

    /**
     *
     */
    public int read() throws IOException {
        log.debug("READ");
        return in.read();
    }

    /**
     *
     */
    public void close() throws IOException {
        log.debug("CLOSE");
        in.close();
    }
}

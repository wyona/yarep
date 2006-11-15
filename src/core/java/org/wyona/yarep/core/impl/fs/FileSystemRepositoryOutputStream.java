package org.wyona.yarep.core.impl.fs;

import org.wyona.yarep.core.Path;
import org.wyona.yarep.core.UID;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.log4j.Category;

/**
 *
 */
public class FileSystemRepositoryOutputStream extends OutputStream {

    private static Category log = Category.getInstance(FileSystemRepositoryOutputStream.class);

    private FileOutputStream out;

    /**
     *
     */
    public FileSystemRepositoryOutputStream(UID uid, Path path, File contentDir) throws IOException {
        try {
            File file = new File(contentDir.getAbsolutePath() + File.separator + uid.toString());
            //File file = new File(contentDir.getAbsolutePath() + path.toString());
            
            File parent = new File(file.getParent());
            if (!parent.exists()) {
               log.warn("Directory will be created: " + parent);
               parent.mkdirs();
            }

            log.debug(file.toString());
            out = new FileOutputStream(file);
        } catch (IOException e) {
            log.error(e);
            throw e;
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

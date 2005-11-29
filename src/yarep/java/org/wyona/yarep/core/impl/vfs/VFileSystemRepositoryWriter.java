package org.wyona.yarep.core.impl.vfs;

import org.wyona.yarep.core.Path;
import org.wyona.yarep.core.UID;

import org.apache.log4j.Category;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

/**
 *
 */
public class VFileSystemRepositoryWriter extends Writer {

    private static Category log = Category.getInstance(VFileSystemRepositoryWriter.class);

    FileWriter writer;

    /**
     *
     */
    public VFileSystemRepositoryWriter(UID uid, Path path, File contentDir) {
        try {
            File file = new File(contentDir.getAbsolutePath() + path.toString());
            log.debug(file.toString());
            File parent = new File(file.getParent());
            if (!parent.exists()) {
               log.warn("Directory will be created: " + parent);
               parent.mkdirs();
            }
            writer = new FileWriter(file);
        } catch (Exception e) {
            log.error(e);
        }
    }

    /**
     *
     */
    public void flush() throws IOException {
        writer.flush();
    }

    /**
     *
     */
    public void write(char cbuf[], int off, int len) throws IOException {
        writer.write(cbuf, off, len);
    }

    /**
     *
     */
    public void write(String str) throws IOException {
        log.debug(str);
        writer.write(str);
    }

    /**
     *
     */
    public void close() throws IOException {
        log.debug("CLOSE");
        writer.close();
    }
}

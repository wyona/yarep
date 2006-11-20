package org.wyona.yarep.core.impl.svn;

import org.wyona.yarep.core.NoSuchNodeException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Category;

/**
 *
 */
public class SVNRepositoryInputStream extends InputStream {

    private static Category log = Category.getInstance(SVNRepositoryInputStream.class);

    private FileInputStream in;

    /**
     *
     */
    public SVNRepositoryInputStream(File file) throws IOException {
        try {
            log.debug(file.toString());
            in = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            log.error(e);
            throw new NoSuchNodeException(e.getMessage());
        } catch (Exception e) {
            log.error(e);
            throw new IOException(e.getMessage());
        }
    }

    /**
     *
     */
    public int read() throws IOException {
        return in.read();
    }

    /**
     *
     */
    public void close() throws IOException {
        in.close();
    }
}

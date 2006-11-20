package org.wyona.yarep.core.impl.svn;

import org.wyona.yarep.core.NoSuchNodeException;
import org.wyona.yarep.core.RepositoryException;

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
    public SVNRepositoryInputStream(File file) throws RepositoryException {
        try {
            log.debug(file.toString());
            in = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            log.error(e);
            throw new NoSuchNodeException(e.getMessage(), e);
        } catch (Exception e) {
            log.error(e);
            throw new RepositoryException(e.getMessage(), e);
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

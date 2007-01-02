package org.wyona.yarep.core.impl.vfs;

import org.wyona.yarep.core.NoSuchNodeException;
import org.wyona.yarep.core.Path;
import org.wyona.yarep.core.RepositoryException;
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

    private InputStream in;

    /**
     *
     */
    public VFileSystemRepositoryInputStream(UID uid, Path path, File contentDir, String alternative) throws RepositoryException {
        try {
            File file = new File(contentDir.getAbsolutePath() + path.toString());
            log.debug(file.toString());

            if (file.exists()) {
                if (file.isFile()) {
                    in = new FileInputStream(file);
                } else {
                    log.warn("Is not a file (is probably a directory): " + file);
                    if(file.isDirectory()) {
                        if (alternative != null) {
                            File altFile = new File(file, alternative);
                            if (altFile.exists()) {
                                log.warn("Alternative file: " + altFile);
                                in = new FileInputStream(altFile);
                            } else {
                                log.warn("No such alternative file: " + altFile);
                                in = new java.io.StringBufferInputStream(getDirectoryListing(file, path));
                            }
                        } else {
                            in = new java.io.StringBufferInputStream(getDirectoryListing(file, path));
                        }
                    } else {
                        in = null;
                        throw new NoSuchNodeException("Is neither file nor a directory: " + file);
                    }
                }
            } else {
                log.warn("No such file or directory: " + file);
                in = null;
                throw new NoSuchNodeException("No such file or directory: " + file);
            }
        } catch (Exception e) {
            log.error(e);
            in = null;
            throw new RepositoryException(e.getMessage(), e);
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

    /**
     *
     */
    public String getDirectoryListing(File file, Path path) {
                            StringBuffer dirListing = new StringBuffer("<?xml version=\"1.0\"?>");
                            dirListing.append("<directory xmlns=\"http://www.wyona.org/yarep/1.0\" path=\""+path+"\" fs-path=\""+file+"\">");
                            String[] children = file.list();
                            for (int i = 0; i < children.length; i++) {
                                dirListing.append("<child name=\"" + children[i] + "\"/>");
                            }
                            dirListing.append("</directory>");
        return dirListing.toString();
    }
}

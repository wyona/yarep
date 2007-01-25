package org.wyona.yarep.impl.repo.fs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.log4j.Category;
import org.wyona.yarep.core.Node;
import org.wyona.yarep.core.RepositoryException;
import org.wyona.yarep.impl.AbstractNode;

/**
 * OutputStream which sets some properties (lastModified, size) to the node 
 * when the stream is closed.
 * 
 * NOTE: Currently not used, because the Node implemenation uses the lastModified and size
 * of the content file.
 */
public class FileSystemOutputStream extends OutputStream {

    private static Category log = Category.getInstance(FileSystemOutputStream.class);

    protected OutputStream out;
    protected Node node;
    protected File file;

    /**
     * 
     */
    public FileSystemOutputStream(Node node, File file) throws FileNotFoundException {
        this.node  = node;
        this.out = new FileOutputStream(file);
        this.file = file;
    }
    
    /**
     * 
     */
    public void write(int b) throws IOException {
        out.write(b);
    }

    /**
     * 
     */
    public void close() throws IOException {
        out.close();
        try {
            node.setProperty(AbstractNode.PROPERTY_SIZE, file.length());
            node.setProperty(AbstractNode.PROPERTY_LAST_MODIFIED, file.lastModified());
        } catch (RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new IOException(e.getMessage());
        }
    }
}

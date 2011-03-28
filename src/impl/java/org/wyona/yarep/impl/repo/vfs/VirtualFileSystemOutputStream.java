package org.wyona.yarep.impl.repo.vfs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.log4j.Logger;
import org.wyona.yarep.core.Node;
import org.wyona.yarep.core.search.Metadata;
import org.wyona.yarep.impl.AbstractNode;

/**
 * OutputStream which sets some properties (lastModified, size) to the node 
 * when the stream is closed.
 * 
 * NOTE: Currently not used, because the Node implemenation uses the lastModified and size
 * of the content file.
 */
public class VirtualFileSystemOutputStream extends OutputStream {

    private static Logger log = Logger.getLogger(VirtualFileSystemOutputStream.class);

    protected OutputStream out;
    protected Node node;
    protected File file;

    /**
     * 
     */
    public VirtualFileSystemOutputStream(Node node, File file) throws FileNotFoundException {
        this.node  = node;
        this.out = new FileOutputStream(file);
        this.file = file;
        log.debug("Write to file: " + file);
    }
    
    /**
     * 
     */
    public void write(int b) throws IOException {
        out.write(b);
    }

    public void flush() throws IOException {
        out.flush();
    }

    public void write(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
    }

    public void write(byte[] b) throws IOException {
        out.write(b);
    }

    /**
     * 
     */
    public void close() throws IOException {
        out.close();
        try {
            //node.setProperty(AbstractNode.PROPERTY_SIZE, file.length());
            node.setProperty(AbstractNode.PROPERTY_LAST_MODIFIED, file.lastModified());

            VirtualFileSystemRepository vfsRepo = ((VirtualFileSystemNode) node).getRepository();
            
            if(vfsRepo.isAutoFulltextIndexingEnabled()) {
                log.debug("Auto fulltext indexing enabled ...");
                vfsRepo.getIndexer().index(node);
            }
        } catch (Exception e) {
            log.error(e, e);
            throw new IOException(e.toString());
        }
    }
}

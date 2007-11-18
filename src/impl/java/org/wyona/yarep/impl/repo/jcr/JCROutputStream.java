package org.wyona.yarep.impl.repo.jcr;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.log4j.Category;

import org.wyona.yarep.core.Node;

/**
 * OutputStream which sets some properties (lastModified, size) to the node 
 * when the stream is closed.
 * 
 * NOTE: Currently not used, because the Node implemenation uses the lastModified and size
 * of the content file.
 */
public class JCROutputStream extends OutputStream {

    private static Category log = Category.getInstance(JCROutputStream.class);

/*
    protected OutputStream out;
    protected Node node;
    protected File file;
*/

    /**
     * 
     */
    public JCROutputStream(Node node) {
/*
        this.node  = node;
        this.out = new FileOutputStream(file);
        this.file = file;
*/
    }
    
    /**
     * 
     */
    public void write(int b) throws IOException {
        log.error("Not implemented yet!");
        //out.write(b);
    }

    /**
     * 
     */
/*
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
*/
}

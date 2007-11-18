package org.wyona.yarep.impl.repo.jcr;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.log4j.Category;

import org.wyona.yarep.impl.repo.jcr.JCRNode;

/**
 * OutputStream which sets some properties (lastModified, size) to the node 
 * when the stream is closed.
 * 
 * NOTE: Currently not used, because the Node implemenation uses the lastModified and size
 * of the content file.
 */
public class JCROutputStream extends OutputStream {

    private static Category log = Category.getInstance(JCROutputStream.class);

    private java.io.ByteArrayOutputStream out;
    private JCRNode node;

    /**
     * 
     */
    public JCROutputStream(JCRNode node) {
        this.node = node;
        this.out = new java.io.ByteArrayOutputStream();
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
        try {
            node.getJCRNode().setProperty("binary-content", new java.io.ByteArrayInputStream(out.toByteArray()));
            out.close();
/*
            node.setProperty(AbstractNode.PROPERTY_SIZE, file.length());
            node.setProperty(AbstractNode.PROPERTY_LAST_MODIFIED, file.lastModified());
*/
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new IOException(e.getMessage());
        }
    }
}

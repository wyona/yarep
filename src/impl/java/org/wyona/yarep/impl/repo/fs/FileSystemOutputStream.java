package org.wyona.yarep.impl.repo.fs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.log4j.Category;
import org.apache.log4j.Logger;
import org.wyona.yarep.core.Node;
import org.wyona.yarep.core.RepositoryException;
import org.wyona.yarep.impl.AbstractNode;
import org.wyona.yarep.impl.repo.vfs.VirtualFileSystemOutputStream;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexWriter;

/**
 * OutputStream which sets some properties (lastModified, size) to the node 
 * when the stream is closed.
 * 
 * NOTE: Currently not used, because the Node implemenation uses the lastModified and size
 * of the content file.
 */
public class FileSystemOutputStream extends OutputStream {

    private static Logger log = Logger.getLogger(FileSystemOutputStream.class);

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
            node.setProperty(AbstractNode.PROPERTY_SIZE, file.length());
            node.setProperty(AbstractNode.PROPERTY_LAST_MODIFIED, file.lastModified());

            String mimeType = node.getMimeType();
            if (mimeType != null) {
                if(log.isDebugEnabled()) log.debug("Mime type: " + mimeType);
                FileSystemRepository fsRepo = ((FileSystemNode) node).getRepository();
                File fulltextSearchIndexFile = fsRepo.getFulltextSearchIndexFile();


                IndexWriter indexWriter = null;
                if (fulltextSearchIndexFile.isDirectory()) {
                    indexWriter = new IndexWriter(fulltextSearchIndexFile.getAbsolutePath(), fsRepo.getAnalyzer(), false);
                } else {
                    indexWriter = new IndexWriter(fulltextSearchIndexFile.getAbsolutePath(), fsRepo.getAnalyzer(), true);
                }
                Document document = new Document();
                // TODO: Use Tika to extract text depending on mime type
                if (mimeType.equals("application/xhtml+xml") || mimeType.equals("application/xml") || mimeType.equals("text/plain") || mimeType.equals("text/html")) {
                    document.add(new Field("_FULLTEXT", new java.io.FileReader(file)));
                    document.add(new Field("_PATH", node.getPath(),Field.Store.YES,Field.Index.UN_TOKENIZED));
                    indexWriter.updateDocument(new org.apache.lucene.index.Term("_PATH", node.getPath()), document);
                } else {
                    log.warn("Indexing of mime type '" + mimeType + "' is not supported yet (path: " + node.getPath() + ")!");
                }
                indexWriter.close();
            } else {
                log.warn("Node " + node.getPath() + " has no mime type associated with and hence will not be indexed!");
            }
        } catch (RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new IOException(e.getMessage());
        }
    }
}

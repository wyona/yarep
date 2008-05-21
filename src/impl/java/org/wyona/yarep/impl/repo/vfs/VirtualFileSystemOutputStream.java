package org.wyona.yarep.impl.repo.vfs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;

import org.apache.log4j.Logger;
import org.wyona.yarep.core.Node;
import org.wyona.yarep.core.RepositoryException;
import org.wyona.yarep.impl.AbstractNode;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexWriter;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.WriteOutContentHandler;
import org.apache.tika.utils.ParseUtils;

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
            //node.setProperty(AbstractNode.PROPERTY_LAST_MODIFIED, file.lastModified());

            String mimeType = node.getMimeType();
            if (mimeType != null) {
                if (log.isDebugEnabled()) log.debug("Mime type: " + mimeType);
                VirtualFileSystemRepository vfsRepo = ((VirtualFileSystemNode) node).getRepository();

                IndexWriter indexWriter = null;
                try {
                    indexWriter = vfsRepo.createFulltextIndexWriter();
                } catch(org.apache.lucene.store.LockObtainFailedException e) {
                    log.warn("Could not init IndexWriter, because of existing lock, hence content of node '" + node.getPath() + "' will not be indexed!");
                    return;
                }
                
                // http://wiki.apache.org/lucene-java/LuceneFAQ#head-917dd4fc904aa20a34ebd23eb321125bdca1dea2
                // http://mail-archives.apache.org/mod_mbox/lucene-java-dev/200607.mbox/%3C092330F8-18AA-45B2-BC7F-42245812855E@ix.netcom.com%3E
                //indexWriter.deleteDocuments(new org.apache.lucene.index.Term("_PATH", node.getPath()));
                //log.debug("Number of deleted documents (" + node.getPath() + "): " + numberOfDeletedDocuments);

                if (indexWriter != null) {
                    Document document = new Document();
                    // Use Tika to extract text depending on mime type:
                    
                    TikaConfig tikaConfig = TikaConfig.getDefaultConfig();
                    // extract text content:
                    Parser parser = tikaConfig.getParser(mimeType);
                    if (parser != null) {
                        StringWriter writer = new StringWriter();
                        String fullText = null;
                        try {
                            parser.parse(node.getInputStream(), new WriteOutContentHandler(writer), new Metadata());
                            fullText = writer.toString();
                            
                            //System.out.println("fulltext: " + fullText);
                        } catch (Exception e) {
                            log.error("Could not index node " + node.getPath() + ": error while extracting text: " + e, e);
                            // don't propagate exception
                        }
        
                        if (fullText != null && fullText.length() > 0) {
                            document.add(new Field("_FULLTEXT", new StringReader(fullText)));
                            //document.add(new Field("_FULLTEXT", fullText, Field.Store.YES, Field.Index.TOKENIZED));
                            document.add(new Field("_PATH", node.getPath(), Field.Store.YES, Field.Index.UN_TOKENIZED));
                            if (log.isDebugEnabled()) log.debug("Node will be indexed: " + node.getPath());
                            indexWriter.updateDocument(new org.apache.lucene.index.Term("_PATH", node.getPath()), document);
                            indexWriter.close();
                            //indexWriter.flush();
                        } else {
                            log.warn("No fulltext has been extracted to index node with mimeType " + mimeType + " (node: " + node.getPath() + ")");
                            indexWriter.close();
                        }
                    } else {
                        log.warn("No parser available to index node with mimeType " + mimeType + " (node: " + node.getPath() + ")");
                        indexWriter.close();
                    }
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("IndexWriter is null and hence node will not be indexed: " + node.getPath());
                    }
log.error("DEBUG: IndexWriter is null and hence node will not be indexed: " + node.getPath());
                }
            }
        } catch (Exception e) {
            log.error(e, e);
            throw new IOException(e.toString());
        }
    }
}

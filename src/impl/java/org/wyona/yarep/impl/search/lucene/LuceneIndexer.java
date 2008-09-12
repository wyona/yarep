package org.wyona.yarep.impl.search.lucene;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.StringReader;
import java.io.StringWriter;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.WriteOutContentHandler;
import org.wyona.yarep.core.Node;
import org.wyona.yarep.core.Property;
import org.wyona.yarep.core.Repository;
import org.wyona.yarep.core.search.Indexer;
import org.wyona.yarep.core.search.Metadata;
import org.wyona.yarep.core.search.SearchException;
import org.wyona.yarep.impl.repo.vfs.VirtualFileSystemNode;
import org.wyona.yarep.impl.repo.vfs.VirtualFileSystemRepository;

/**
 * Lucene implementation of indexer
 */
public class LuceneIndexer implements Indexer {
    
    static Logger log = Logger.getLogger(LuceneIndexer.class);
    protected LuceneConfig config;
    
    public void configure(Configuration searchIndexConfig, File configFile, Repository repo) throws SearchException {
        this.config = new LuceneConfig(searchIndexConfig, configFile.getParent(), repo);
    }
    
    public void index(Node node) throws SearchException {
        index(node, (Metadata)null);
    }
    
    public void index(Node node, Metadata metaData) throws SearchException{
        try {
            org.apache.tika.metadata.Metadata tikaMetaData = new org.apache.tika.metadata.Metadata();
            if (metaData != null) {
                log.warn("TODO: Copy name/value pairs");
            }
            String mimeType = node.getMimeType();
            if (mimeType != null) {
                if (log.isDebugEnabled()) log.debug("Mime type: " + mimeType);

                IndexWriter indexWriter = null;
                try {
                    indexWriter = createFulltextIndexWriter();
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
                    
                    // Extract/parse text content:
                    Parser parser = config.getTikaConfig().getParser(mimeType);
                    if (parser != null) {
                        StringWriter writer = new StringWriter();
                        String fullText = null;
                        try {
                            tikaMetaData.set("yarep-path", node.getPath());
                            parser.parse(node.getInputStream(), new WriteOutContentHandler(writer), tikaMetaData);
                            fullText = writer.toString();
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
                }
            }
        } catch (Exception e) {
            log.error(e, e);
            throw new SearchException(e.toString());
        }
    }
    
    /* (non-Javadoc)
     * @see org.wyona.yarep.core.search.Indexer#removeFromIndex(org.wyona.yarep.core.Node)
     */
    public void removeFromIndex(Node node) {
        IndexWriter indexWriter = null;
        VirtualFileSystemRepository vfsRepo = ((VirtualFileSystemNode) node).getRepository();
        String nodePath = "Could not get Path of node.";
        try {
            nodePath = node.getPath();
            indexWriter = createFulltextIndexWriter();
            indexWriter.deleteDocuments(new org.apache.lucene.index.Term("_PATH", node.getPath()));
            indexWriter.close();
        } catch(Exception e) {
            log.warn("Could not init IndexWriter, because of existing lock, hence content of node '" + nodePath + "' will not be deleted from the index!");
            try {
                indexWriter.close();
            } catch (Exception e2) {
                log.warn("Could not close indexWriter. Exception: " + e2.getMessage());
            }
        }
    }
    
    /**
     *
     */
   public IndexWriter createFulltextIndexWriter() throws Exception {
        return createIndexWriter(config.getFulltextSearchIndexFile(), config.getFulltextAnalyzer());
       // IMPORTANT: This doesn't work within a clustered environment!
       //return this.indexWriter;
   }
   
   /**
    *
    */
   public IndexWriter createPropertiesIndexWriter() throws Exception {
       return createIndexWriter(config.getPropertiesSearchIndexFile(), config.getPropertyAnalyzer());
       // IMPORTANT: This doesn't work within a clustered environment!
       //return this.propertiesIndexWriter;
   }
   
   /**
    * Init an IndexWriter
    * @param indexDir Directory where the index is located
    */
   private IndexWriter createIndexWriter(File indexDir, Analyzer analyzer) throws Exception {
       IndexWriter iw = null;
       if (indexDir != null) {
           //TODO: if (indexDir.isDirectory()) is probably not needed, try catch (FileNotFoundException e) should be enough
           if (indexDir.isDirectory()) {
               try {
                   iw = new IndexWriter(indexDir.getAbsolutePath(), analyzer, false);
               } catch (FileNotFoundException e) {
                   //probably it got an instance of the writer and didn't index anything so it's missing the segemnts files. 
                   //create a new index  
                   iw = new IndexWriter(indexDir.getAbsolutePath(), analyzer, true);
               }
           } else {
               iw = new IndexWriter(indexDir.getAbsolutePath(), analyzer, true);
           }
           // TODO: iw.setWriteLockTimeout(long ms)
           //log.debug("Max write.lock timeout: " + iw.getDefaultWriteLockTimeout() + " milliseconds");
           return iw;
       }
       return null;
   }

   public void index(Node node, Property property) throws SearchException {
       index(node, property, (Metadata)null);
   }
   
   public void index(Node node, Property property, Metadata metadata) throws SearchException {
       try {
           Document luceneDoc = new Document();
           if (property.getValueAsString() != null) {
               // Add the property to the lucene document
               // TODO: write typed property value to index. Is this actually possible?
               //log.debug("Index property '" + property.getName() + "': " + property.getValueAsString());
               luceneDoc.add(new Field(property.getName(), property.getValueAsString(), Field.Store.YES, Field.Index.UN_TOKENIZED));
           } else {
               log.warn("Property '" + property.getName() + "' has null as value and hence will not be indexed (path: " + node.getPath() + ")!");
           }

           // Add path as field such that found properties can be related to a path
           luceneDoc.add(new Field("_PATH", node.getPath(), Field.Store.YES, Field.Index.UN_TOKENIZED));
           IndexWriter iw = null;
           try {
               iw = createPropertiesIndexWriter();
           } catch(org.apache.lucene.store.LockObtainFailedException e) {
               log.warn("Could not init IndexWriter, because of existing lock, hence properties of node '" + node.getPath() + "' will not be indexed!");
               return;
           }
           if (iw != null) {
               iw.updateDocument(new org.apache.lucene.index.Term("_PATH", node.getPath()), luceneDoc);
               // Make sure to close the IndexWriter and release the lock!
               iw.close();
               //iw.flush();
               if (log.isDebugEnabled()) log.debug("Index node: " + node.getPath());
           } else {
               if (log.isDebugEnabled()) {
                   log.debug("No property index configured, hence do not index properties of node: " + node.getPath());
               }
           }
       } catch (Exception e) {
           log.error("Could not index property.",e);
           throw new SearchException();
       }
   }
   
   public void removeFromIndex(Node node, Property property) throws SearchException {
       log.warn("TODO: Not implemented yet.");
   }
}

package org.wyona.yarep.impl.search.lucene;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.StringReader;
import java.io.StringWriter;

import org.apache.avalon.framework.configuration.Configuration;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.TermQuery;

import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
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
 * Version 2 of Lucene implementation of indexer (mixing fulltext and properties)
 */
public class LuceneIndexerV2 implements Indexer {
    
    static Logger log = LogManager.getLogger(LuceneIndexerV2.class);
    protected LuceneConfig config;

    private static final String SYNC_LOCK_PROPERTIES = "sync-lock-properties";
    private static final String SYNC_LOCK_FULLTEXT = "sync-lock-fulltext";

    /**
     * @see org.wyona.yarep.core.search.Indexer#configure(Configuration, File, Repository)
     */
    public void configure(Configuration searchIndexConfig, File configFile, Repository repo) throws SearchException {
        this.config = new LuceneConfig(searchIndexConfig, configFile.getParent(), repo);
    }
    
    /**
     * @see org.wyona.yarep.core.search.Indexer#index(Node)
     */
    public void index(Node node) throws SearchException {
        if (log.isDebugEnabled()) {
            try {
                log.debug("Index fulltext of node: " + node.getPath());
            } catch(Exception e) {
                log.warn(e, e);
            }
        }
        index(node, (Metadata)null);
    }
    
    /**
     * @see org.wyona.yarep.core.search.Indexer#index(Node, Metadata)
     */
    public void index(Node node, Metadata metaData) throws SearchException {
        try {
            String path = node.getPath();
            if (config.doIndexRevisions() && org.wyona.yarep.util.YarepUtil.isRevision(node)) {
                String revisionName = ((org.wyona.yarep.core.Revision)node).getRevisionName();
                log.debug("Trying to index revision: " + path + " (" + revisionName + "), " + node.getClass().getName());
                path = path + "#revision=" + revisionName; // TODO: Discuss the separator
            } else {
                log.debug("Trying to index node: " + path);
            }

            if (metaData != null) {
                log.warn("This indexer implementation '" + getClass().getName() + "' is currently not making use of the meta data argument!");
            }

            Document luceneDoc = getDocument(path);

            // INFO: Add fulltext and tika properties
            String mimeType = node.getMimeType();
            if (mimeType != null) {
                if (log.isDebugEnabled()) log.debug("Mime type: " + mimeType);
                luceneDoc = addFulltext(node, mimeType, luceneDoc);
            } else {
                log.warn("Node '" + path + "' has no mime-type set and hence actual node content will not be added to fulltext index.");
            }

            // INFO: Add properties
            Property[] properties = node.getProperties();
            if (properties != null) {
                for (int i = 0; i < properties.length; i++) {
                    //log.debug("Add property to fulltext index: " + properties[i].getName());
                    if (properties[i].getValueAsString() != null) {
                        luceneDoc.add(new Field(properties[i].getName(), properties[i].getValueAsString(), Field.Store.YES, Field.Index.TOKENIZED));
                    }
                }
            } else {
                log.info("Node '" + path + "' has no properties.");
            }

            // INFO: Update index
            try {
                log.debug("Fulltext index: Add/update node: " + path);
                updateDocument(createFulltextIndexWriter(), path, luceneDoc);
            } catch(org.apache.lucene.store.LockObtainFailedException e) {
                log.warn("Could not init 'fulltext' IndexWriter (maybe because of existing lock, exception message: " + e.getMessage() + "), hence content of node '" + path + "' will not be indexed!");
                // TODO: log node path into dedicated log message!
            }
        } catch (Exception e) {
            log.error(e, e);
            throw new SearchException(e.toString());
        }
    }

    /**
     * @see org.wyona.yarep.core.search.Indexer#removeFromIndex(org.wyona.yarep.core.Node)
     */
    public void removeFromIndex(Node node) throws SearchException {
        try {
            removeFromFulltextIndex(node.getPath());
        } catch(org.wyona.yarep.core.RepositoryException e) {
            // TODO: Check whether it makes sense to throw a SearchException... (WARN: Backwards compatibility!)
            log.error(e, e);
        }
    }

    /**
     * Remove document with a specific path from fulltext search index
     * @param path Path of entry
     */
    private void removeFromFulltextIndex(String path) {
        log.debug("Trying to remove document '" + path + "' from fulltext index...");
        IndexWriter indexWriter = null;
        try {
            indexWriter = createFulltextIndexWriter();
            if (indexWriter != null) {
                indexWriter.deleteDocuments(new org.apache.lucene.index.Term("_PATH", path));
                indexWriter.close();
            } else {
                log.warn("No fulltext index writer, hence could not remove document '" + path + "' from fulltext index!");
            }
        } catch(Exception e) {
            log.warn("Probably IndexWriter could not be initialized, because of existing lock, hence node with path '" + path + "' will not be deleted from the index! Exception message: " + e.getMessage());
            //log.error(e, e);
            try {
                indexWriter.close();
            } catch (Exception e2) {
                log.warn("Could not close indexWriter. Exception message: " + e2.getMessage());
                //log.error(e2, e2);
            }
        }
    }
    
    /**
     * Get index writer
     */
   protected IndexWriter createFulltextIndexWriter() throws Exception {
       synchronized(SYNC_LOCK_FULLTEXT) {
           log.debug("Fulltext search index directory: " + config.getFulltextSearchIndexFile());
           return createIndexWriter(config.getFulltextSearchIndexFile(), config.getFulltextAnalyzer(), config.getWriteLockTimeout());
           // IMPORTANT: This doesn't work within a clustered environment!
           //return this.indexWriter;
       }
   }

   /**
    * Create writer for modifying properties
    */
   protected IndexWriter createPropertiesIndexWriter() throws Exception {
       synchronized(SYNC_LOCK_PROPERTIES) {
           log.debug("Properties search index directory: " + config.getPropertiesSearchIndexFile());
           return createIndexWriter(config.getPropertiesSearchIndexFile(), config.getPropertyAnalyzer(), config.getWriteLockTimeout());
           // IMPORTANT: This doesn't work within a clustered environment!
           //return this.propertiesIndexWriter;
       }
   }
   
    /**
     * Init an Index Writer
     * @param indexDir Directory where the index (segment files) is located
     * @param analyzer TODO
     * @param timeout write.lock timeout
     */
    static IndexWriter createIndexWriter(File indexDir, Analyzer analyzer, long timeout) throws Exception {
       IndexWriter.setDefaultWriteLockTimeout(timeout); // INFO: According to https://issues.apache.org/jira/browse/LUCENE-621 (initiated by http://www.gossamer-threads.com/lists/lucene/java-dev/37421) one can set the write.lock timeout ahead of initializing an IndexWriter
       log.debug("Configured write.lock timeout: " + IndexWriter.getDefaultWriteLockTimeout() + "ms");

       if (indexDir != null) {
           log.debug("Try to init IndexWriter based on index directory: " + indexDir.getAbsolutePath());
           if (indexDir.isDirectory()) {
               IndexWriter iw = null;
               try {
                   iw = new IndexWriter(indexDir.getAbsolutePath(), analyzer, false);
               } catch (FileNotFoundException e) {
                   log.warn("Index directory '" + indexDir.getAbsolutePath() + "' seems to exist, but probably no segment files yet!");
                   iw = new IndexWriter(indexDir.getAbsolutePath(), analyzer, true);
               }
               return iw;
           } else {
               indexDir.mkdirs();
               log.warn("Index directory has been created: " + indexDir.getAbsolutePath());
               return new IndexWriter(indexDir.getAbsolutePath(), analyzer, true);
           }
       } else {
           log.error("No index directory set!");
           return null;
       }
    }

    /**
     * @see org.wyona.yarep.core.search.Indexer#index(Node, Property)
     */
    public void index(Node node, Property property) throws SearchException {
        try {
            String path = node.getPath();
            if (config.doIndexRevisions() && org.wyona.yarep.util.YarepUtil.isRevision(node)) {
                String revisionName = ((org.wyona.yarep.core.Revision)node).getRevisionName();
                log.debug("Index property '" + property.getName() + " of revision: " + path + " (" + revisionName + "), " + node.getClass().getName());
                path = path + "#revision=" + revisionName; // TODO: Discuss the separator
            } else {
                log.debug("Index property '" + property.getName() + " of node: " + path);
            }

            Document luceneDoc = getDocument(path);

            // TODO: Write typed property value to index. Is this actually possible?
            // INFO: As workaround Add the property as string value to the lucene document
            if (property.getValueAsString() != null) {
                log.debug("Index property '" + property.getName() + "': " + property.getValueAsString());
                //luceneDoc.add(new Field(property.getName(), new StringReader(property.getValueAsString())));
                luceneDoc.add(new Field(property.getName(), property.getValueAsString(), Field.Store.YES, Field.Index.TOKENIZED));
            } else {
                log.warn("Property '" + property.getName() + "' has null as string value and hence will not be indexed (path: " + path + ")!");
            }

            // INFO: Re-add all other properties of node to lucene doc, whereas this is just a workaround, because TermDocs/IndexReader does not work (please see below)
            Property[] properties = node.getProperties();
            for (int i = 0; i < properties.length; i++) {
                if (!properties[i].getName().equals(property.getName())) {
                    if (properties[i].getValueAsString() != null) {
                        luceneDoc.add(new Field(properties[i].getName(), properties[i].getValueAsString(), Field.Store.YES, Field.Index.TOKENIZED));
                    }
                }
            }

            // INFO: Now add lucene document containing all properties to index
            try {

/* INFO/WARN: The following code is/was a workaround for not having to wait for the timeout (see http://www.gossamer-threads.com/lists/lucene/java-dev/37421), but it seems to remove the write.lock mysteriously by itself!
                if(!IndexWriter.isLocked(new org.apache.lucene.store.SimpleFSDirectory(config.getPropertiesSearchIndexFile()))) { // INFO: Do not wait for timeout, but rather take action right away...
                    updateDocument(createPropertiesIndexWriter(), path, luceneDoc);
                } else {
                    log.warn("Index 'properties' is locked, hence properties of node '" + path + "' will not be indexed!");
                    // TODO: log node path into dedicated log message!
                }
*/
                log.debug("Properties index: Add/update property '" + property.getName() + "' (Value: " + property.getValueAsString() + ") of node: " + path);
                updateDocument(createPropertiesIndexWriter(), path, luceneDoc);
            } catch(org.apache.lucene.store.LockObtainFailedException e) {
                log.warn("Could not init 'properties' IndexWriter (maybe because of existing lock (Timeout: " + IndexWriter.getDefaultWriteLockTimeout() + "ms), exception message: " + e.getMessage() + "), hence properties of node '" + path + "' will not be indexed!");
                // TODO: log node path into dedicated log message!
            }
        } catch (Exception e) {
            log.error(e, e);
            throw new SearchException(e.getMessage());
        }
    }
  
    /**
     * @see org.wyona.yarep.core.search.Indexer#removeFromIndex(org.wyona.yarep.core.Node, Property)
     */
    public void removeFromIndex(Node node, Property property) throws SearchException {
        try {
            String path = node.getPath();
            log.debug("Trying to remove property '" + property.getName() + "' of node '" + path + "' from properties index...");
            IndexWriter indexWriter = null;
            try {
                indexWriter = createPropertiesIndexWriter();
                indexWriter.deleteDocuments(new org.apache.lucene.index.Term("_PATH", path)); // TODO: Actually only documents with _PATH = path and containing a field with the property name should be deleted!
                indexWriter.close();
            } catch(Exception e) {
                log.warn("Probably IndexWriter could not be initialized, because of existing lock, hence node with path '" + path + "' will not be deleted from the index! Exception message: " + e.getMessage());
                //log.error(e, e);
                try {
                    indexWriter.close();
                } catch (Exception e2) {
                    log.warn("Could not close indexWriter. Exception message: " + e2.getMessage());
                    //log.error(e2, e2);
                }
            }
        } catch(org.wyona.yarep.core.RepositoryException e) {
            log.error(e, e);
        }
    }

    /**
     * Update document of a particular path within index
     *
     * @param indexWriter Index writer
     * @param path Path of node with which the fields and values are related to
     * @param document Lucene document containing the new fields and new values
     */
    private void updateDocument(IndexWriter indexWriter, String path, Document document) throws Exception {
        Term pathTerm = new Term("_PATH", path);

        if (indexWriter != null) {
            if (log.isDebugEnabled()) log.debug("Node '" + path + "' will be indexed.");
            indexWriter.updateDocument(pathTerm, document);
            indexWriter.optimize(); // TODO: Make this configurable because of performance problem?!
            indexWriter.close();
            //indexWriter.flush();
        } else {
            throw new Exception("Index writer is null and hence node '" + path + "' will not be indexed!");
            //log.warn("IndexWriter is null and hence node will not be indexed: " + path);
        }
    }

    /**
     * Init lucene document
     * @param path Node path for which fields and values are associated with
     */
    private Document getDocument(String path) {
        Document luceneDoc = new Document();
        // INFO: Add path as field such that found properties can be related to a path
        luceneDoc.add(new Field("_PATH", path, Field.Store.YES, Field.Index.UN_TOKENIZED));
        return luceneDoc;
    }

    /**
     * Add fulltext to lucene document
     */
    private Document addFulltext(Node node, String mimeType, Document luceneDoc) throws Exception {
        String fullText = null;

        // Extract/parse text content:
        Parser parser = config.getTikaConfig().getParser(mimeType);
        if (parser != null) {
            try {
                org.apache.tika.metadata.Metadata tikaMetaData = new org.apache.tika.metadata.Metadata();
                tikaMetaData.set("yarep-path", node.getPath());

                StringWriter writer = new StringWriter();

/*
                            // The WriteOutContentHandler writes all character content out to the writer. Please note that Tika also contains various other utility classes to extract content, such as for example the BodyContentHandler (see http://lucene.apache.org/tika/apidocs/org/apache/tika/sax/package-summary.html)
                            parser.parse(node.getInputStream(), new WriteOutContentHandler(writer), tikaMetaData);
                            // WARN: See http://www.mail-archive.com/tika-dev@lucene.apache.org/msg00743.html
                            log.warn("Fulltext generation with WriteOutContentHandler does seem to be buggy (because title and body are not separated with a space): " + fullText);
*/

                // NOTE: The body content handler generates xhtml ... instead just the words ...
                parser.parse(node.getInputStream(), new BodyContentHandler(writer), tikaMetaData);
                fullText = writer.toString();
                writer.close();
/* INFO: Alternative to using a writer ...
                            BodyContentHandler textHandler = new BodyContentHandler();
                            parser.parse(node.getInputStream(), textHandler, tikaMetaData);
                            fullText = textHandler.toString();
                            log.debug("Body: " + fullText);
*/

                log.debug("Remove all html tags: " + fullText);
                 fullText = fullText.replaceAll("\\<.*?>", " "); // INFO: Please make sure to replace by space, because otherwise words get concatenated and hence cannot be found anymore!
                //fullText = fullText.replaceAll("\\<.*?>", " ").replace("&#160;", " ");
                log.debug("Without HTML tags: " + fullText);

                // TODO: Add more meta content to full text
                String title = tikaMetaData.get(org.apache.tika.metadata.Metadata.TITLE);
                if (title != null && title.trim().length() > 0) {
                    fullText = fullText + " " + title;
                }

                String keywords = tikaMetaData.get(org.apache.tika.metadata.Metadata.KEYWORDS);
                if (keywords != null && keywords.trim().length() > 0) fullText = fullText + " " + keywords;

                String description = tikaMetaData.get(org.apache.tika.metadata.Metadata.DESCRIPTION);
                if (description != null && description.trim().length() > 0) fullText = fullText + " " + description;

                //log.debug("debug: Fulltext including title and meta: " + fullText);
                if (fullText != null && fullText.length() > 0) {
                    //luceneDoc.add(new Field(LuceneIndexer.INDEX_PROPERTY_FULL, new StringReader(fullText))); // INFO: http://lucene.apache.org/java/2_0_0/api/org/apache/lucene/document/Field.html#Field(java.lang.String,%20java.io.Reader)
                    //luceneDoc.add(new Field(LuceneIndexer.INDEX_PROPERTY_FULL, fullText, Field.Store.NO, Field.Index.TOKENIZED));
                    luceneDoc.add(new Field(LuceneIndexer.INDEX_PROPERTY_FULL, fullText, Field.Store.YES, Field.Index.TOKENIZED));
                } else {
                    log.warn("No fulltext has been extracted to index node with mimeType " + mimeType + " (node: " + node.getPath() + ")");
                }

                for (int i = 0; i < tikaMetaData.names().length; i++) {
                    String tikaPropName = tikaMetaData.names()[i];
                    if (tikaMetaData.isMultiValued(tikaPropName)) {
                        log.warn("Tika property is multi valued: " + tikaPropName);
                    }
                    luceneDoc.add(new Field("tika_" + tikaPropName, tikaMetaData.get(tikaPropName), Field.Store.YES, Field.Index.TOKENIZED));
                }
            } catch (Exception e) {
                log.error("Could not index node " + node.getPath() + ": error while extracting text: " + e, e);
                // INFO: Don't throw exception in order to be fault tolerant
            }
        } else {
            log.warn("No parser available to index node with mimeType " + mimeType + " (node: " + node.getPath() + ")");
        }

        return luceneDoc;
    }
}

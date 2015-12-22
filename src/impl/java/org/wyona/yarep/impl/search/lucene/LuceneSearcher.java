package org.wyona.yarep.impl.search.lucene;

import java.util.ArrayList;
import java.util.List;

import org.apache.avalon.framework.configuration.Configuration;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.apache.lucene.search.IndexSearcher;

import org.wyona.yarep.core.NoSuchNodeException;
import org.wyona.yarep.core.Node;
import org.wyona.yarep.core.Repository;
import org.wyona.yarep.core.search.SearchException;
import org.wyona.yarep.core.search.Searcher;

import java.io.File;

/**
 * Lucene implementation of searcher
 */
public class LuceneSearcher implements Searcher {
    
    static Logger log = LogManager.getLogger(LuceneSearcher.class);

    private LuceneConfig config;

    private static final String PATH_FIELD_NAME = "_PATH";

    private boolean autoClean = false;
  
    /**
     * @see org.wyona.yarep.core.search.Searcher#configure(Configuration, File, Repository)
     */  
    public void configure(Configuration searchIndexConfig, File configFile, Repository repo) throws SearchException {
        this.config = new LuceneConfig(searchIndexConfig, configFile.getParent(), repo);
    }
    
    /**
     * @see org.wyona.yarep.core.search.Searcher#search(String)
     */
    public Node[] search(String query) throws SearchException {
        try {
            //TODO: this is not really nice re performance, it reads the index form the file-system for each search
            //it would be nice to initialize IndexSearcher at startup and reuse the IndexSearcher 
            //but in this case the IndexSearcher then uses the index as it was at startup and not reloading it when the index has changed at runtime
            org.apache.lucene.search.Searcher searcher = new IndexSearcher(config.getFulltextSearchIndexFile().getAbsolutePath());
            if (searcher != null) {
                try {
                    org.apache.lucene.search.Query luceneQuery = new org.apache.lucene.queryParser.QueryParser(LuceneIndexer.INDEX_PROPERTY_FULL, config.getFulltextAnalyzer()).parse(query);
                    org.apache.lucene.search.Hits hits = searcher.search(luceneQuery);
                    log.info("Query \"" + query + "\" returned " + hits.length() + " hits");

                    java.util.List<Node> results = new java.util.ArrayList<Node>();
                    for (int i = 0; i < hits.length();i++) {
                        String path = hits.doc(i).getField(PATH_FIELD_NAME).stringValue();
                        if (path.contains("#revision=")) {
                            //log.debug("This seems to be a revision: " + resultPath);
                            String resultPathWithoutRevision = path.substring(0, path.lastIndexOf("#revision="));
                            String revisionName = path.substring(path.lastIndexOf("#revision=") + 10);
                            if (config.getRepo().existsNode(resultPathWithoutRevision)) {
                                try {
                                    results.add(config.getRepo().getNode(resultPathWithoutRevision).getRevision(revisionName));
                                } catch(org.wyona.yarep.core.NoSuchRevisionException e) {
                                    log.error("Revision found within search index, but no such revision within repository: " + resultPathWithoutRevision + "#" + revisionName);
                                }
                            } else {
                                log.error("Node found within search index, but no such node within repository: " + resultPathWithoutRevision);
                            }
                        } else {
                            if (config.getRepo().existsNode(path)) {
                                results.add(config.getRepo().getNode(path));
                            } else {
                                log.error("No such node '" + path + "'. Search index (Fulltext: '" + config.getFulltextSearchIndexFile() + "', Properties: '" + config.getPropertiesSearchIndexFile() + "') seems to be out of sync!");
                            }
                        }
                    }
                    searcher.close();
                    return results.toArray(new Node[results.size()]);
                } catch (Exception e) {
                    searcher.close();
                    log.error(e, e);
                    throw new SearchException(e.getMessage(),e);
                }
            } else {
                searcher.close();
                log.warn("No search index seems to be configured!");
            }
        } catch (Exception e) {
            log.error(e, e);
            throw new SearchException(e.getMessage(),e);
        }
        return null;
    }

    /**
     * @see org.wyona.yarep.core.search.Searcher#searchProperty(String, String, String)
     */
    public Node[] searchProperty(String pName, String query, String path) throws SearchException {
        try {
            //TODO: this is not really nice re performance, it reads the index form the file-system for each search
            //it would be nice to initialize IndexSearcher at startup and reuse the IndexSearcher 
            //but in this case the IndexSearcher then uses the index as it was at startup and not reloading it when the index has changed at runtime            
            org.apache.lucene.search.Searcher searcher = new IndexSearcher(config.getPropertiesSearchIndexFile().getAbsolutePath());
            if (searcher != null) {
                try {
                    log.debug("Search property '" + pName + "': " + query);

                    String defaultField = pName;
                    org.apache.lucene.queryParser.QueryParser queryParser = new org.apache.lucene.queryParser.QueryParser(defaultField, config.getPropertyAnalyzer());
                    org.apache.lucene.search.Query luceneQuery = queryParser.parse(query);

                    org.apache.lucene.search.Hits hits = searcher.search(luceneQuery);
                    log.info("Number of matching documents (Property: " + pName + ", Query: " + query + ", Path: " + path + "): " + hits.length());
                    List results = new ArrayList();
                    for (int i = 0; i < hits.length(); i++) {
                        try {
                            String resultPath = hits.doc(i).getField(PATH_FIELD_NAME).stringValue();

                            // subtree filter (WARN: Peformance/Scalability!)
                            if (path == null) {
                                path = "/";
                                log.warn("No scope path set, hence set it ROOT: " + path);
                            }
                            if (resultPath.startsWith(path)) {
                                if (resultPath.contains("#revision=")) {
                                    //log.debug("This seems to be a revision: " + resultPath);
                                    String resultPathWithoutRevision = resultPath.substring(0, resultPath.lastIndexOf("#revision="));
                                    String revisionName = resultPath.substring(resultPath.lastIndexOf("#revision=") + 10);
                                    if (config.getRepo().existsNode(resultPathWithoutRevision)) {
                                        try {
                                            results.add(config.getRepo().getNode(resultPathWithoutRevision).getRevision(revisionName));
                                        } catch(org.wyona.yarep.core.NoSuchRevisionException e) {
                                            log.error("Revision found within search index, but no such revision within repository: " + resultPathWithoutRevision + "#" + revisionName);
                                        }
                                    } else {
                                        log.error("Node found within search index, but no such node within repository: " + resultPathWithoutRevision);
                                    }
                                } else {
                                    if (config.getRepo().existsNode(resultPath)) {
                                        results.add(config.getRepo().getNode(resultPath));
                                    } else {
                                        log.debug("Node found within search index, but no such node within repository: " + resultPath);
                                        if(autoClean) {
                                            // TODO: Remove entry from index
                                        }
                                    }
                                }
                            } else {
                                log.warn("Scope path '" + path + "' did not match result path: " + resultPath);
                            }
                        } catch (NoSuchNodeException nsne) { // INFO: I think catching this exception is not really necessary anymore. because the code above already checks the existence...
                            log.warn("Node found within search index, but no such node within repository: " + hits.doc(i).getField(PATH_FIELD_NAME).stringValue());
                            if(autoClean) {
                                // TODO: Remove entry from index
                            }
                        }
                    }
                    searcher.close();

                    return (Node[])results.toArray(new Node[results.size()]);
                } catch (Exception e) {
                    log.error(e, e);
                    throw new SearchException(e.getMessage(),e);
                }
            }
        } catch (Exception e) {
            log.error(e, e);
            throw new SearchException(e.getMessage(),e);
        }
        return null;
    }

    /**
     * Get list of paths of of nodes and/or revisions, which do not exist anymore inside repository
     * @param delete Flag to indicate whether nodes which are missing inside the repository should be deleted from the index
     * @param limitSize Limit the size of the returned list of missing nodes, because a search index can contain a huge amount of documents and hence also a huge amount of missing nodes, which means if one does not set a limit, then it might take a very long time to generate this list. If the limit of size is set to -1, then this means no limit.
     * @return List of paths of of nodes and/or revisions, which do not exist anymore inside repository
     */
    public String[] getMissingNodes(boolean delete, int limitSize) throws SearchException {
        try {
            File indexDirectory = config.getPropertiesSearchIndexFile();
            org.apache.lucene.search.Searcher searcher = new IndexSearcher(indexDirectory.getAbsolutePath());
            if (searcher != null) {
                try {
                    org.apache.lucene.search.Query luceneQuery = new org.apache.lucene.search.MatchAllDocsQuery();

                    org.apache.lucene.search.Hits hits = searcher.search(luceneQuery);
                    log.warn("DEBUG: Number of documents: " + hits.length() + " (Index directory: " + indexDirectory.getAbsolutePath() + ")");
                    log.info("Number of documents: " + hits.length() + " (Index directory: " + indexDirectory.getAbsolutePath() + ")");

                    List<String> results = new ArrayList<String>();
                    for (int i = 0; i < hits.length(); i++) {
                        String resultPath = hits.doc(i).getField(PATH_FIELD_NAME).stringValue();
                        try {
                            if (resultPath.contains("#revision=")) {
                                //log.debug("This seems to be a revision: " + resultPath);
                                String resultPathWithoutRevision = resultPath.substring(0, resultPath.lastIndexOf("#revision="));
                                String revisionName = resultPath.substring(resultPath.lastIndexOf("#revision=") + 10);
                                if (config.getRepo().existsNode(resultPathWithoutRevision)) {
                                    try {
                                        config.getRepo().getNode(resultPathWithoutRevision).getRevision(revisionName);
                                    } catch(org.wyona.yarep.core.NoSuchRevisionException e) {
                                        log.error("Revision found within search index and node itself exists inside repository, but no such revision within repository: " + resultPathWithoutRevision + "#" + revisionName);
                                        results.add(resultPath);
                                    }
                                } else {
                                    log.error("Revision '" + resultPath + "' found within search index, but no such node within repository: " + resultPathWithoutRevision);
                                    results.add(resultPath);
                                }
                            } else {
                                if (!config.getRepo().existsNode(resultPath)) {
                                    log.error("Node found within search index, but no such node within repository: " + resultPath);
                                    results.add(resultPath);
                                }
                            }
                        } catch (NoSuchNodeException nsne) { // INFO: I think catching this exception is not really necessary anymore. because the code above already checks the existence...
                            log.warn("Node found within search index, but no such node within repository: " + resultPath);
                            results.add(resultPath);
                        }

                        if (limitSize > 0 && results.size() == limitSize) {
                            log.warn("Size of returned list of missing nodes has been limited to '" + limitSize + "'");
                            break;
                        }
                    }
                    searcher.close();

                    if (delete) {
                        log.warn("Delete missing documents from index...");
                        try {
                            // TODO: Use Indexer configured by repository!
                            org.apache.lucene.index.IndexWriter indexWriter = LuceneIndexerV2.createIndexWriter(indexDirectory, config.getPropertyAnalyzer(), config.getWriteLockTimeout());
                            if (indexWriter != null) {
                                for (String path: results) {
                                    log.warn("DEBUG: Try to delete document from index: " + path);
                                    indexWriter.deleteDocuments(new org.apache.lucene.index.Term(PATH_FIELD_NAME, path));
                                }
                                indexWriter.close();
                            } else {
                                throw new SearchException("Could not init IndexWriter in order to delete missing documents!");
                            }
                        } catch(Exception e) {
                            throw new SearchException(e);
                        }
                    }

                    return (String[])results.toArray(new String[results.size()]);
                } catch (Exception e) {
                    log.error(e, e);
                    throw new SearchException(e.getMessage(),e);
                }
            } else {
                log.error("Searcher is null!");
                return null;
            }
        } catch (Exception e) {
            log.error(e, e);
            throw new SearchException(e.getMessage(),e);
        }
    }
}

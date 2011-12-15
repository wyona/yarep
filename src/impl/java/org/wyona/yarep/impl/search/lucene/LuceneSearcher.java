package org.wyona.yarep.impl.search.lucene;

import java.util.ArrayList;
import java.util.List;

import org.apache.avalon.framework.configuration.Configuration;

import org.apache.log4j.Logger;

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
    
    static Logger log = Logger.getLogger(LuceneSearcher.class);

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
                                        log.error("Node found within search index, but no such node within repository: " + resultPath);
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
     * @return List of paths of of nodes and/or revisions, which do not exist anymore inside repository
     */
    public String[] getMissingNodes(boolean delete) throws SearchException {
        try {
            org.apache.lucene.search.Searcher searcher = new IndexSearcher(config.getPropertiesSearchIndexFile().getAbsolutePath());
            if (searcher != null) {
                try {
                    org.apache.lucene.search.Query luceneQuery = new org.apache.lucene.search.MatchAllDocsQuery();

                    org.apache.lucene.search.Hits hits = searcher.search(luceneQuery);
                    log.warn("DEBUG: Number of documents: " + hits.length());
                    log.info("Number of documents: " + hits.length());

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
                                    results.add(resultPathWithoutRevision);
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
                    }
                    searcher.close();

                    if (delete) {
                        log.warn("Delete from index...");
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

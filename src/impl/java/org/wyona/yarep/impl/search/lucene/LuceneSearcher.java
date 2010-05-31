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
    
    public void configure(Configuration searchIndexConfig, File configFile, Repository repo) throws SearchException {
        this.config = new LuceneConfig(searchIndexConfig, configFile.getParent(), repo);
    }
    
    /**
     * Search content
     */
    public Node[] search(String query) throws SearchException {
        try {
            //TODO: this is not really nice re performance, it reads the index form the file-system for each search
            //it would be nice to initialize IndexSearcher at startup and reuse the IndexSearcher 
            //but in this case the IndexSearcher then uses the index as it was at startup and not reloading it when the index has changed at runtime
            org.apache.lucene.search.Searcher searcher = new IndexSearcher(config.getFulltextSearchIndexFile().getAbsolutePath());
            if (searcher != null) {
                try {
                    org.apache.lucene.search.Query luceneQuery = new org.apache.lucene.queryParser.QueryParser("_FULLTEXT", config.getFulltextAnalyzer()).parse(query);
                    org.apache.lucene.search.Hits hits = searcher.search(luceneQuery);
                    log.info("Query \"" + query + "\" returned " + hits.length() + " hits");
                    Node[] results = new Node[hits.length()];
                    for (int i = 0; i < results.length;i++) {
                        String path = hits.doc(i).getField("_PATH").stringValue();
                        if (config.getRepo().existsNode(path)) {
                            results[i] = config.getRepo().getNode(path);
                        } else {
                            log.error("No such node '" + path + "'. Search index (Fulltext: '" + config.getFulltextSearchIndexFile() + "', Properties: '" + config.getPropertiesSearchIndexFile() + "') seems to be out of sync!");
                        }
                    }
                    searcher.close();
                    return results;
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
     * Search property
     *
     * @param pName Property name
     * @param query Search query
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
                    org.apache.lucene.search.Query luceneQuery = new org.apache.lucene.queryParser.QueryParser(pName, config.getPropertyAnalyzer()).parse(query);
                    org.apache.lucene.search.Hits hits = searcher.search(luceneQuery);
                    log.info("Number of matching documents: " + hits.length());
                    List results = new ArrayList();
                    for (int i = 0; i < hits.length(); i++) {
                        try {
                            String resultPath = hits.doc(i).getField("_PATH").stringValue();
                            
                            // subtree filter
                            if (resultPath.startsWith(path)) {
                                results.add(config.getRepo().getNode(resultPath));
                            }
                        } catch (NoSuchNodeException nsne) {
                            log.warn("Found within search index, but no such node within repository: " + hits.doc(i).getField("_PATH").stringValue());
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
}

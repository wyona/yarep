package org.wyona.yarep.core.search;

import org.apache.avalon.framework.configuration.Configuration;
import org.wyona.yarep.core.Node;
import org.wyona.yarep.core.Repository;

import java.io.File;

/**
 *  Search Interface
 */
public interface Searcher {

    /**
     * Configure searcher
     *
     * @param searchIndexConfig The part of the yarep repository configuration containing the search/index configuration
     * @param configFile The file where the yarep repository configuration is located (useful when resolving relative paths of referenced files). TODO: Maybe replace later with a resolver!
     * @param repo Yarep Repository which contains content related to index which is searched in
     */
    public void configure(Configuration searchIndexConfig, File configFile, Repository repo) throws SearchException;
    
    /**
     * Search content
     *
     * @param query Search query
     */
    public Node[] search(String query) throws SearchException;
    
    /**
     * Search for properties content within subtree
     *
     * @param pName Property name
     * @param query Search query
     * @param path Scope of search (path of subtree, in order to search the whole tree one sets path = "/")
     */
    public Node[] searchProperty(String pName, String query, String path) throws SearchException;
}

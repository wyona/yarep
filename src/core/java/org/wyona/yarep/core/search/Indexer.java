package org.wyona.yarep.core.search;

import org.apache.avalon.framework.configuration.Configuration;
import org.wyona.yarep.core.Node;
import org.wyona.yarep.core.Property;
import org.wyona.yarep.core.Repository;

import java.io.File;

/**
 *  Search indexer interface
 */
public interface Indexer {

    /**
     * Configure indexer
     *
     * @param searchIndexConfig The part of the yarep repository configuration containing the search/index configuration
     * @param configFile The file where the yarep repository configuration is located (useful when resolving relative paths of referenced files, for example Tika config). TODO: Maybe replace later with a resolver!
     * @param repo Yarep Repository which contains content to be indexed
     */
    public void configure(Configuration searchIndexConfig, File configFile, Repository repo) throws SearchException;
    
    /**
     * (Re-)Index content of node
     *
     * @param node Node
     */
    public void index(Node node) throws SearchException;
    
    /**
     * (Re-)Index property of node
     * @param node Node
     * @param property Property of node
     */
    public void index(Node node, Property property) throws SearchException;

    /**
     * (Re-)Index content of node. Meta data can be useful for parser selection or additional content to be indexed which is not contained by the node itself
     * @param node Yarep content node
     * @param metadata Meta data containing additional information which might be useful for indexing, but is not contained by the node itself
     */
    public void index(Node node, Metadata metadata) throws SearchException;

    /**
     * Removes node from index
     * @param node Node
     */
    public void removeFromIndex(Node node) throws SearchException;
    
    /**
     * Removes property of node from index
     * @param node Node
     * @param property Property
     */
    public void removeFromIndex(Node node, Property property) throws SearchException;
}

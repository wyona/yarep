package org.wyona.yarep.tests;

import java.io.File;

import org.wyona.yarep.core.Repository;
import org.wyona.yarep.core.RepositoryFactory;

/**
 * Test for the node based repository implementation.
 */
public class TreeNodeRepoTest extends NodeRepoTest {

    public void setUp() throws Exception {
        RepositoryFactory repoFactory = new RepositoryFactory();
        repo = repoFactory.newRepository("tree-node-fs-example", new File(
                "tree-node-fs-example/repository.xml"));
    }
    
    protected String getCollectionTestPath() {
        return "/00110047";
    }
    
    protected String getResourceTestName() {
        return "world.txt";
    }
    
    protected String getResourceTestPath() {
        return getCollectionTestPath() + "/" + getResourceTestName();
    }
    
    protected String getRevisionTestPath() {
        return getCollectionTestPath() + "/revisiontest.txt";
    }

    
}

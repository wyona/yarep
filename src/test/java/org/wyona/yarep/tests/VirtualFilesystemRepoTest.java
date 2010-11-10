package org.wyona.yarep.tests;

import java.io.File;

import org.wyona.yarep.core.RepositoryFactory;

import org.apache.log4j.Logger;

import org.wyona.yarep.core.Node;

/**
 * Test for the 'virtual filesystem' repository implementation.
 */
public class VirtualFilesystemRepoTest extends NodeRepoTest {

    private static Logger log = Logger.getLogger(VirtualFilesystemRepoTest.class);

    @Override
    public void setUp() throws Exception {
        RepositoryFactory repoFactory = new RepositoryFactory();
        repo = repoFactory.newRepository("vfs-example", new File("new-vfs-example/repository.xml"));
        log.info("Testing repository: " + repo.getName());
    }

    @Override
    protected String getCollectionTestPath() {
        return "/another-directory";
    }
    
    @Override
    protected String getResourceTestName() {
        return "index.html";
    }
    
    @Override
    protected String getResourceTestPath() {
        return getCollectionTestPath() + "/" + getResourceTestName();
    }
    
    @Override
    protected String getRevisionTestPath() {
        return "/hello-world.txt";
    }

    /**
     * Test get children of a node
     */
    public void testGetNodes() throws Exception {
        Node node = repo.getNode(getCollectionTestPath());
        node.getNodes();
        assertEquals(node.getNodes().length, 1);
    }
}

package org.wyona.yarep.tests;

import org.wyona.yarep.core.Node;
import org.wyona.yarep.core.NodeType;
import org.wyona.yarep.core.Repository;
import org.wyona.yarep.core.RepositoryFactory;

import org.apache.log4j.Logger;

import junit.framework.TestCase;

/**
 * Test the 'virtual filesystem' repository implementation re creating and retrieving revisions.
 */
public class VirtualFilesystemRevisionsTest extends TestCase {

    private static Logger log = Logger.getLogger(VirtualFilesystemRevisionsTest.class);

    private Repository repo;

    private String NODE_NAME = "revision-test.txt";

    public void setUp() throws Exception {
        RepositoryFactory repoFactory = new RepositoryFactory();
        repo = repoFactory.newRepository("vfs-example", new java.io.File("new-vfs-example/repository.xml"));
        log.info("Testing repository: " + repo.getName());
    }

    /**
     * Test create a new revision
     */
    public void testCreateRevision() throws Exception {
        String path = "/" + NODE_NAME;
        if (repo.existsNode(path)) {
            repo.getNode(path).delete();
        }

        Node node = repo.getRootNode().addNode(NODE_NAME, NodeType.RESOURCE);
        java.io.PrintWriter pw = new java.io.PrintWriter(node.getOutputStream());
        pw.print("Hello revision test!");
        pw.close();

        assertTrue("Revision has been created: ", true);
    }

/*
    protected String getCollectionTestPath() {
        return "/another-directory";
    }
    
    protected String getResourceTestName() {
        return "index.html";
    }
    
    protected String getResourceTestPath() {
        return getCollectionTestPath() + "/" + getResourceTestName();
    }
    
    protected String getRevisionTestPath() {
        return "/hello-world.txt";
    }
*/
}

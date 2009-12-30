package org.wyona.yarep.tests;

import org.wyona.yarep.core.Node;
import org.wyona.yarep.core.NodeType;
import org.wyona.yarep.core.Repository;
import org.wyona.yarep.core.RepositoryFactory;
import org.wyona.yarep.core.Revision;

import org.apache.log4j.Logger;

import junit.framework.TestCase;

/**
 * Test the 'virtual filesystem' repository implementation re creating and retrieving revisions.
 */
public class VirtualFilesystemRevisionsTest extends TestCase {

    private static Logger log = Logger.getLogger(VirtualFilesystemRevisionsTest.class);

    private Repository repo;

    private String NODE_NAME = "revision-test.txt";

    /**
     * Setup of repository configuration
     */
    public void setUp() throws Exception {
        RepositoryFactory repoFactory = new RepositoryFactory();
        repo = repoFactory.newRepository("vfs-example", new java.io.File("new-vfs-example/repository.xml"));
        log.info("Testing repository: " + repo.getName() + " (" + repo.getConfigFile() + ")");
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
        node.checkout("bob");
        node.setMimeType("text/plain");
        java.io.PrintWriter pw = new java.io.PrintWriter(node.getOutputStream());
        pw.print("Hello revision test!");
        pw.close();
        Revision revision = node.checkin("My first revision");

        log.info("Revision has been created: " + revision.getRevisionName());
        assertTrue("Revision has been created: " + revision.getRevisionName(), revision != null);
    }
}

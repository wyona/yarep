package org.wyona.yarep.tests;

import org.wyona.yarep.core.Node;
import org.wyona.yarep.core.NodeType;
import org.wyona.yarep.core.Repository;
import org.wyona.yarep.core.RepositoryFactory;
import org.wyona.yarep.core.Revision;

import org.apache.log4j.Logger;

import junit.framework.TestCase;

import java.util.Date;

/**
 * Test the 'virtual filesystem' repository implementation re indexing and searching.
 */
public class VirtualFilesystemIndexSearchTest extends TestCase {

    private static Logger log = Logger.getLogger(VirtualFilesystemIndexSearchTest.class);

    private Repository repo;

    private String NODE_NAME = "hello-world.txt";

    /**
     * Setup of repository configuration
     */
    public void setUp() throws Exception {
        RepositoryFactory repoFactory = new RepositoryFactory();
        repo = repoFactory.newRepository("new-vfs"); // INFO: Make sure that this repo is configured within 'src/test/java/yarep.properties'
        //repo = repoFactory.newRepository("vfs-example", new java.io.File("new-vfs-example/repository.xml"));
        log.info("Testing repository: " + repo.getName() + " (" + repo.getConfigFile() + ")");
    }

    /**
     * Test indexing and searching of properties
     */
    public void testIndexingSearchingOfProperties() throws Exception {
        String path = "/" + NODE_NAME;

        String name = "firstnames";
        String value = "bob alice";

        Node node = repo.getNode(path);
        node.setProperty(name, value);
        Node[] nodes = repo.getSearcher().searchProperty(name, "alice", "/");
        log.info("Number of nodes found: " + nodes.length);

        assertTrue(nodes != null && nodes.length == 1 && nodes[0].getPath().equals(path));
    }
}
